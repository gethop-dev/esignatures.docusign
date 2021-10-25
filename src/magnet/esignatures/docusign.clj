;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns magnet.esignatures.docusign
  (:require [integrant.core :as ig]
            [magnet.esignatures.docusign.api :as api]
            [magnet.esignatures.docusign.oauth :as oauth]))

(defn init-record [config]
  (-> config
      (update-in [:auth-config :private-key] oauth/load-private-key)
      (api/map->DocuSign)))

(defmethod ig/init-key :magnet.esignature/docusign [_ config]
  (init-record config))
