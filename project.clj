(defproject dev.gethop/esignatures.docusign "0.1.6-SNAPSHOT"
  :description "A library for interacting with the DocuSign eSignature API"
  :url "https://github.com/gethop-dev/esignatures.docusign"
  :license {:name "Mozilla Public License 2.0"
            :url "https://www.mozilla.org/en-US/2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [buddy/buddy-sign "3.4.1"]
                 [integrant "0.8.0"]
                 [http-kit "2.3.0"]
                 [diehard "0.10.4"]]
  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/CLOJARS_USERNAME
                                      :password :env/CLOJARS_PASSWORD
                                      :sign-releases false}]
                        ["releases"  {:url "https://clojars.org/repo"
                                      :username :env/CLOJARS_USERNAME
                                      :password :env/CLOJARS_PASSWORD
                                      :sign-releases false}]]
  :profiles {:dev [:project/dev :profiles/dev]
             :profiles/dev {}
             :project/dev {:plugins [[jonase/eastwood "1.2.3"]
                                     [lein-cljfmt "0.8.0"]]}
             :repl {:repl-options {:init-ns dev.gethop.esignatures.core
                                   :host "0.0.0.0"
                                   :port 4001}}
             :eastwood {:linters [:all]}})
