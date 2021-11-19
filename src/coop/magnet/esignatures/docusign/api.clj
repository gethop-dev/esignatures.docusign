;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns coop.magnet.esignatures.docusign.api
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [coop.magnet.esignatures.core :as core]
            [coop.magnet.esignatures.docusign.oauth :as oauth]
            [coop.magnet.esignatures.util :as util])
  (:import [java.io ByteArrayOutputStream]
           [java.io BufferedInputStream]
           [java.util Base64]))

(defn input-stream->base64 [^BufferedInputStream input-stream]
  (with-open [is input-stream
              out (ByteArrayOutputStream.)]
    (io/copy is out)
    (.encodeToString (Base64/getEncoder) (.toByteArray out))))

(defn- document->api-document
  [{:keys [name file-extension stream]}]
  {:name name
   :fileExtension file-extension
   :documentBase64 (input-stream->base64 stream)})

(defn- add-ids [id-key entities]
  (map-indexed
   (fn [idx entity]
     (assoc entity id-key (inc idx)))
   entities))

(defn- documents->api-documents [documents]
  (->> documents
       (map document->api-document)
       (add-ids :documentId)))

(defn- signer->api-signer [signer]
  (set/rename-keys signer {:id :clientUserId}))

(defn- signers->api-signers [signers]
  (->> signers
       (map signer->api-signer)
       (add-ids :recipientId)))

(defn- envelope->api-envelope
  [{:keys [documents signers]}]
  {:documents (documents->api-documents documents)
   :recipients {:signers (signers->api-signers signers)}
   :emailSubject "Please sign this document set"
   :status "sent"})

(s/fdef create-envelope
  :args ::core/create-envelope-args
  :ret  ::core/create-envelope-ret)

(defn create-envelope
  [{:keys [base-url account-id retry-config] :as adapter} envelope opts]
  {:pre [(s/valid? ::core/envelope envelope)
         (s/valid? ::core/create-envelope-opts opts)]}
  (if-let [access-token (oauth/get-access-token adapter)]
    (let [api-envelope (envelope->api-envelope envelope)
          {:keys [status body]}
          (util/do-request {:method :post
                            :url (format "%s/restapi/v2.1/accounts/%s/envelopes" base-url account-id)
                            :headers {"Authorization" (str "Bearer " access-token)}
                            :body (merge api-envelope opts)}
                           retry-config)]
      (if (and (= status :ok) (:envelopeId body))
        {:success? true :id (:envelopeId body)}
        {:success? false
         :reason :request-failed
         :error-details {:body body}}))
    {:success? false
     :reason :could-not-get-access-token}))

(s/fdef delete-signing-url
  :args ::core/delete-envelope-signing-url-args
  :ret  ::core/delete-envelope-signing-url-ret)

(defn delete-envelope
  [{:keys [base-url account-id retry-config] :as adapter} envelope-id opts]
  {:pre [(s/valid? ::core/envelope-id envelope-id)
         (s/valid? ::core/delete-envelope-opts opts)]}
  (if-let [access-token (oauth/get-access-token adapter)]
    (let [{:keys [status body]}
          (util/do-request {:method :put
                            :url (format "%s/restapi/v2.1/accounts/%s/envelopes/%s"
                                         base-url account-id envelope-id)
                            :headers {"Authorization" (str "Bearer " access-token)}
                            :body (merge
                                   {:status "voided"
                                    :voidedReason  "API delete"}
                                   opts)}
                           retry-config)]
      (if (= status :ok)
        {:success? true}
        {:success? false
         :reason :request-failed
         :error-details {:status status
                         :body body}}))
    {:success? false
     :reason :could-not-get-access-token}))

(s/fdef get-signing-url
  :args ::core/get-envelope-signing-url-args
  :ret  ::core/get-envelope-signing-url-ret)

(defn get-envelope-signing-url
  [{:keys [retry-config base-url account-id] :as adapter} envelope-id {:keys [id email name] :as signer} return-url opts]
  {:pre [(s/valid? ::core/envelope-id envelope-id)
         (s/valid? ::core/signer signer)
         (s/valid? ::core/return-url return-url)
         (s/valid? ::core/get-envelope-signing-url-opts opts)]}
  (if-let [access-token (oauth/get-access-token adapter)]
    (let [view {:returnUrl return-url
                :authenticationMethod "none"
                :email email
                :userName name
                :clientUserId id}
          {:keys [status body]}
          (util/do-request {:method :post
                            :url (format "%s/restapi/v2.1/accounts/%s/envelopes/%s/views/recipient"
                                         base-url account-id envelope-id)
                            :headers {"Authorization" (str "Bearer " access-token)}
                            :body (merge view opts)}
                           retry-config)]
      (if (and (= status :ok) (:url body))
        {:success? true :url (:url body)}
        {:success? false
         :reason :request-failed
         :error-details {:body body}}))
    {:success? false
     :reason :could-not-get-access-token}))

(defn get-envelope-documents
  [{:keys [retry-config base-url account-id] :as adapter} envelope-id opts]
  (if-let [access-token (oauth/get-access-token adapter)]
    (let [mode (get opts :documentId "combined")
          {:keys [status body]}
          (util/do-request {:method :get
                            :url (format "%s/restapi/v2.1/accounts/%s/envelopes/%s/documents/%s"
                                         base-url account-id envelope-id mode)
                            :headers {"Authorization" (str "Bearer " access-token)}}
                           retry-config)]
      (if (and (= status :ok) body)
        {:success? true
         :documents body}
        {:success? false
         :reason :request-failed
         :error-details {:body body}}))
    {:success? false
     :reason :could-not-get-access-token}))

(defrecord DocuSign [auth-config base-url account-id retry-config]
  core/ESignature
  (create-envelope [this envelope]
    (create-envelope this envelope {}))
  (create-envelope [this envelope opts]
    (create-envelope this envelope opts))
  (delete-envelope [this envelope-id]
    (delete-envelope this envelope-id {}))
  (delete-envelope [this envelope-id opts]
    (delete-envelope this envelope-id opts))
  (get-envelope-signing-url [this envelope-id signer return-url]
    (get-envelope-signing-url this envelope-id signer return-url {}))
  (get-envelope-signing-url [this envelope-id signer return-url opts]
    (get-envelope-signing-url this envelope-id signer return-url opts))
  (get-envelope-documents [this envelope-id]
    (get-envelope-documents this envelope-id {}))
  (get-envelope-documents [this envelope-id opts]
    (get-envelope-documents this envelope-id opts)))
