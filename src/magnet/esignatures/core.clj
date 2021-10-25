;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns magnet.esignatures.core
  (:require [clojure.spec.alpha :as s])
  (:import [java.io InputStream]))

(s/def ::id string?)
(s/def ::name string?)
(s/def ::email string?)
(s/def ::file-extension string?)
(s/def ::stream #(instance? InputStream %))
(s/def ::document (s/keys :req-n [::name
                                  ::file-extension
                                  ::stream]))
(s/def ::documents (s/coll-of ::document :min-count 1))

(s/def ::signer (s/keys :req-un [::id
                                 ::email
                                 ::name]))
(s/def ::signers (s/coll-of ::signer :min-count 1))

(s/def ::envelope (s/keys :req-un [::documents
                                   ::signers]))
(s/def ::envelope-id string?)
(s/def ::return-url string?)
(s/def ::success? boolean?)

(s/def ::create-envelope-opts map?)
(s/def ::create-envelope-args (s/cat :config record?
                                     :envelope ::envelope
                                     :opts (s/? ::create-envelope-opts)))
(s/def ::create-envelope-ret (s/keys :req-un [::success?]
                                     :opt-un [::envelope-id]))

(s/def ::get-envelope-signing-url-opts map?)
(s/def ::get-envelope-signing-url-args (s/cat :config record?
                                              :envelope-id ::envelope-id
                                              :signer ::signer
                                              :return-url ::return-url
                                              :opts (s/? ::get-envelope-signing-url-opts)))
(s/def ::get-envelope-signing-url-ret (s/keys :req-un [::success?]
                                              :opt-un [::envelope-id]))

(defprotocol ESignature
  (create-envelope
    [this envelope]
    [this envelope opts])
  (get-envelope-signing-url
    [this envelope-id signer return-url]
    [this envelope-id signer return-url opts])
  (get-envelope-documents
    [this envelope-id]
    [this envelope-id opts]))
