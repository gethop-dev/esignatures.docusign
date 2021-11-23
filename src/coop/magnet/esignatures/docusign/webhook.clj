;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns coop.magnet.esignatures.docusign.webhook
  (:import [java.lang String]
           [java.security MessageDigest]
           [java.util Base64]
           [javax.crypto Mac]
           [javax.crypto.spec SecretKeySpec]))

(def ^:const algorithm "HmacSHA256")

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
