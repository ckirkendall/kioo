(defproject kioo "0.5.1-SNAPSHOT"
  :description "enlive/enfocus style templating for Facebook's React."
  :url "http://github.com/ckirkendall/kioo"
  :author "Creighton Kirkendall"
  :min-lein-version "2.0.0"
  :lein-release {:deploy-via :clojars}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[enlive "1.1.6" :exclusions [org.jsoup/jsoup]]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [cljsjs/react-dom "16.3.2-0"]
                 [cljsjs/create-react-class "15.6.2-0"]
                 [com.googlecode.htmlcompressor/htmlcompressor "1.5.2"]
                 [sablono "0.8.4" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server org.omcljs/om]]
                 [hickory "0.7.1"]
                 [org.omcljs/om "1.0.0-beta2" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                 [reagent "0.8.0" :exclusions [cljsjs/react cljsjs/react-dom cljsjs/react-dom-server]]
                 [enlive-ws "0.1.1"]]
  :profiles {:dev {:plugins [[lein-cljsbuild "1.1.7"]
                             [lein-doo "0.1.10"]
                             [lein-shell "0.5.0"]
                             [lein-ancient "0.6.15"]
                             [lein-marginalia "0.9.1"]
                             [com.jakemccrary/lein-test-refresh "0.22.0"]]}}

  :aliases {"auto-test-clj" ["test-refresh"]
            "auto-test-cljs" ["do" "clean"
                              ["doo" "chrome-headless" "test"]]}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {:output-to "target/dev/kioo.js"
                                   :optimizations :none
                                   :pretty-print true
                                   :source-map true
                                   :infer-externs true
                                   :install-deps true
                                   :npm-deps {:react "~16.3.2"
                                              :react-dom "~16.3.2"
                                              :react-dom-factories "~1.0.2"
                                              :create-react-class "~15.6.2"}}}
                       {:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/test/kioo.js"
                                   :optimizations :none
                                   :main kioo.test-runner
                                   :pretty-print true
                                   :infer-externs true
                                   :install-deps true
                                   :npm-deps {:react "~16.3.2"
                                              :react-dom "~16.3.2"
                                              :react-dom-factories "~1.0.2"
                                              :create-react-class "~15.6.2"}}}]}
  :resource-paths ["test-resources"]
  :source-paths ["src"]
  :test-paths ["test"])
