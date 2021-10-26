;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns coop.magnet.esignatures.docusign.webhook
  (:import [java.lang String]
           [java.security MessageDigest]
           [javax.crypto Mac]
           [javax.crypto.spec SecretKeySpec]))

(def ^:const algorithm "HmacSHA256")

(defn- compute-hmac [^String secret ^String payload]
  (->
   (doto (Mac/getInstance algorithm)
     (.init (SecretKeySpec. (.getBytes secret) algorithm)))
   (.doFinal (.getBytes payload))))

(defn signature-valid? [secret payload ^String signature]
  (let [digest1 (compute-hmac secret payload)
        digest2 (.getBytes signature "utf-8")]
    (MessageDigest/isEqual digest1 digest2)))