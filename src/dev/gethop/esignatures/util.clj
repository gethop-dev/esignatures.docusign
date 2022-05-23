;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns dev.gethop.esignatures.util
  (:require [cheshire.core :as json]
            [diehard.core :as dh]
            [org.httpkit.client :as http])
  (:import [java.lang Exception]
           [org.httpkit.client TimeoutException]))

(def ^:const default-timeout
  "Default timeout value for an connection attempt."
  5000)

(def ^:const default-max-retries
  "Default limit of request attempts."
  10)

(def ^:const default-initial-delay
  "Initial delay for retries, specified in milliseconds."
  500)

(def ^:const default-max-delay
  "Maximun delay for a connection retry, specified in milliseconds. We
  are using truncated binary exponential backoff, with `max-delay` as
  the ceiling for the retry delay."
  1000)

(def ^:const default-backoff-ms
  [default-initial-delay default-max-delay 2.0])

(defn- fallback [_value exception]
  (let [status (condp instance? exception
                 ;; Socket layer related exceptions
                 java.net.UnknownHostException :unknown-host
                 java.net.ConnectException :connection-refused
                 ;; HTTP layer related exceptions
                 org.httpkit.client.TimeoutException :gateway-timeout
                 org.httpkit.client.AbortException :bad-gateway)]
    {:status status}))

(defn- retry-policy [max-retries backoff-ms]
  (dh/retry-policy-from-config
   {:max-retries max-retries
    :backoff-ms backoff-ms
    :retry-on [org.httpkit.client.TimeoutException
               org.httpkit.client.AbortException]}))

(defn- parse-response-body [{:keys [headers body]}]
  (try
    (let [content-type (get headers :content-type)]
      (if (and (re-find #"application/json" content-type)
               (string? body))
        (json/parse-string body true)
        {:content-type content-type :content body}))
    (catch Exception _
      nil)))

(defn- http-status-code->status [status-code]
  (cond
    (<= 200 status-code 299) :ok

    (= 400 status-code) :bad-request
    (= 401 status-code) :unauthorized
    (= 403 status-code) :forbidden
    (= 404 status-code) :not-found
    (= 409 status-code) :conflict
    (<= 400 status-code 499) :unknown-client-error

    (= 500 status-code) :internal-server-error
    (= 502 status-code) :bad-gateway
    (= 504 status-code) :gateway-timeout
    (<= 400 status-code 499) :unknown-server-error

    :else :unknown))

(defn parse-response [{:keys [status] :as request}]
  (let [status (if (number? status)
                 (http-status-code->status status)
                 status)
        body (parse-response-body request)]
    {:status status :body body}))

(defn do-request [request {:keys [timeout max-retries backoff-ms]
                           :or {timeout default-timeout
                                max-retries default-max-retries
                                backoff-ms default-backoff-ms}}]
  (dh/with-retry {:policy (retry-policy max-retries backoff-ms)
                  :fallback fallback}
    (let [request (cond-> request
                    (:body request)
                    (->
                     (update :body json/generate-string)
                     (assoc-in [:headers "Content-Type"] "application/json"))
                    :always
                    (assoc :timeout timeout))
          {:keys [status error] :as response} @(http/request request)]
      (when error
        (throw error))
      (when (= status 504)
        (throw (TimeoutException. "Server 504")))
      (parse-response response))))
