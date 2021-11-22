(defproject coop.magnet/esignatures.docusign "0.1.3-SNAPSHOT"
  :description "A library for interacting with the DocuSign eSignature API"
  :url "https://github.com/magnetcoop/esignatures.docusign"
  :license {:name "Mozilla Public License 2.0"
            :url "https://www.mozilla.org/en-US/2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [buddy/buddy-sign "3.4.1"]
                 [integrant "0.8.0"]
                 [http-kit "2.3.0"]
                 [diehard "0.10.4"]]
  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]
  :profiles {:dev [:project/dev :profiles/dev]
             :profiles/dev {}
             :project/dev {:plugins [[jonase/eastwood "0.9.9"]
                                     [lein-cljfmt "0.8.0"]]}
             :repl {:repl-options {:init-ns coop.magnet.esignatures.core
                                   :host "0.0.0.0"
                                   :port 4001}}})
