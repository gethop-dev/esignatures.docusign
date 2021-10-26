;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns coop.magnet.esignatures.util
  (:require [cheshire.core :as json]
            [diehard.core :as dh]
            [org.httpkit.client :as http])
  (:import [java.lang Exception]
           [org.httpkit.client TimeoutException]))

(def ^:const default-timeout
  "Default timeout value for an connection attempt with Stripe API."
  5000)

(def ^:const default-max-retries
  "Default limit of attempts for Stripe request."
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

(def ^:const gateway-timeout
  "504 Gateway timeout The server, while acting as a gateway or proxy,
  did not receive a timely response from the upstream server specified
  by the URI (e.g. HTTP, FTP, LDAP) or some other auxiliary
  server (e.g. DNS) it needed to access in attempting to complete the
  request."
  504)

(def ^:const bad-gateway
  "502 Bad gateway The server, while acting as a gateway or proxy,
  received an invalid response from the upstream server it accessed in
  attempting to fulfill the request."
  502)

(defn- fallback [_value exception]
  (let [status (condp instance? exception
                 ;; Socket layer related exceptions
                 java.net.UnknownHostException :unknown-host
                 java.net.ConnectException :connection-refused
                 ;; HTTP layer related exceptions
                 org.httpkit.client.TimeoutException gateway-timeout
                 org.httpkit.client.AbortException bad-gateway)]
    {:status status}))

(defn- retry-policy [max-retries backoff-ms]
  (dh/retry-policy-from-config
   {:max-retries max-retries
    :backoff-ms backoff-ms
    :retry-on [org.httpkit.client.TimeoutException
               org.httpkit.client.AbortException]}))

(defn- parse-response-body [{:keys [headers body]}]
  (let [content-type (get headers :content-type)]
    (if (and (re-find #"application/json" content-type)
             (string? body))
      (json/parse-string body true)
      {:content-type content-type :content body})))

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
          {:keys [status error] :as request} @(http/request request)]
      (when error
        (throw error))
      (when (= status 504)
        (throw (TimeoutException. "Server 504")))
      (try
        {:status status
         :body (parse-response-body request)}
        (catch Exception e
          {:status bad-gateway})))))
