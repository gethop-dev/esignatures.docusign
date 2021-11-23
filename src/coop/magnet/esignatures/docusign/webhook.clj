;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns coop.magnet.esignatures.docusign.webhook
  (:require [clojure.string :as str])
  (:import [java.lang String]
           [java.security MessageDigest]
           [java.util Base64]
           [javax.crypto Mac]
           [javax.crypto.spec SecretKeySpec]))

(def ^:const algorithm "HmacSHA256")
(def ^:const signature-header-pattern #"^x-docusign-signature-[0-9]+$")

(defn- ^String encode-base64 [src]
  (.encodeToString (Base64/getEncoder) src))

(defn- ^String compute-hmac [^String secret ^String payload]
  (->
   (doto (Mac/getInstance algorithm)
     (.init (SecretKeySpec. (.getBytes secret) algorithm)))
   (.doFinal (.getBytes payload))
   (encode-base64)))

(defn signature-valid? [^String secret ^String payload ^String signature]
  (let [digest1 (.getBytes (compute-hmac secret payload) "utf-8")
        digest2 (.getBytes signature "utf-8")]
    (MessageDigest/isEqual digest1 digest2)))

(defn signature-header-key? [header]
  (->> (name header)
       (str/lower-case)
       (re-matches signature-header-pattern)
       (boolean)))

(defn valid-headers? [^String secret ^String payload headers]
  (->> headers
       (keep (fn [[k v]] (when (signature-header-key? k) v)))
       (some (partial signature-valid? secret payload))))
