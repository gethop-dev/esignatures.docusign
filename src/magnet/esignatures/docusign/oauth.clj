;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns magnet.esignatures.docusign.oauth
  (:require [buddy.sign.jwt :as jwt]
            [clojure.string :as str]
            [magnet.esignatures.util :as util])
  (:import [java.security KeyFactory]
           [java.security.spec PKCS8EncodedKeySpec]
           [java.util Base64]))

(defn- clean-private-key [plain-private-key]
  (-> plain-private-key
      (str/replace #"\n" "")
      (str/replace #"-----BEGIN RSA PRIVATE KEY-----" "")
      (str/replace #"-----END RSA PRIVATE KEY-----" "")
      (str/trim)))

(defn load-private-key [plain-private-key]
  (let [clean-key (clean-private-key plain-private-key)
        buffer (.decode (Base64/getDecoder) clean-key)
        spec (PKCS8EncodedKeySpec. buffer)
        factory (KeyFactory/getInstance "RSA")]
    (.generatePrivate factory spec)))

(defn- generate-jwt
  [{:keys [integration-key user-id auth-service-uri private-key]}]
  (let [claims {:iss integration-key
                :sub user-id
                :iat (System/currentTimeMillis)
                :exp (+ 300 (quot (System/currentTimeMillis) 1000))
                :aud (str/replace auth-service-uri #"https://" "")
                :scope "signature"}
        opts {:alg :rs256
              :header {:typ "JWT"}}]
    (jwt/sign claims private-key opts)))

(defn get-access-token [{:keys [auth-config retry-config]}]
  (when-let [jwt-token (generate-jwt auth-config)]
    (let [params {:grant_type "urn:ietf:params:oauth:grant-type:jwt-bearer"
                  :assertion jwt-token}
          result (util/do-request {:method :post
                                   :url (format "%s/oauth/token" (:auth-service-uri auth-config))
                                   :form-params params}
                                  retry-config)]
      (get (:body result) :access_token))))
