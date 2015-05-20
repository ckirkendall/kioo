(defproject kioo "0.4.1-SNAPSHOT"
  :description "enlive/enfocus style templating for Facebook's React."
  :url "http://github.com/ckirkendall/kioo"
  :author "Creighton Kirkendall"
  :min-lein-version "2.0.0"
  :lein-release {:deploy-via :clojars}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[enlive "1.1.5"]
                 [com.facebook/react "0.11.1"]
                 [cljsjs/react "0.13.1-0"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [com.googlecode.htmlcompressor/htmlcompressor "1.5.2"]
                 [sablono "0.2.20"]
                 [sablono "0.3.4"]
                 [hickory "0.5.3"]
                 [om "0.7.1"]
                 [reagent "0.4.2"]
                 [enlive-ws  "0.1.1"]]
  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
            [lein-shell "0.4.0"]]
  :aliases
  {"auto-test"
   ["do" "clean"
    ["shell" "mkdir" "-p" "target/test-classes"]
    ["cljx"]
    ["cljsbuild" "auto" "test"]]}
  :cljsbuild {:builds []}
  :profiles {:dev {:plugins [[com.keminglabs/cljx "0.3.2"] ;; Must be before Austin: https://github.com/cemerick/austin/issues/37
                             #_[com.cemerick/austin "0.1.3"]
                             [com.cemerick/clojurescript.test "0.3.2-SNAPSHOT"]
                             [lein-cljsbuild "1.0.4-SNAPSHOT"]
                             [lein-ancient "0.5.4"]]
                   :hooks [cljx.hooks leiningen.cljsbuild]
                   :cljx {:builds [{:source-paths ["src"]
                                    :output-path "target/classes"
                                    :rules :clj}
                                   {:source-paths ["src"]
                                    :output-path "target/classes"
                                    :rules :cljs}
                                   {:source-paths ["test"]
                                    :output-path "target/test-classes"
                                    :rules :clj}
                                   {:source-paths ["test"]
                                    :output-path "target/test-classes"
                                    :rules :cljs}]}
                   :cljsbuild {:builds [#_{:id "dev"
                                         :source-paths ["test" "target/classes" "target/test-classes"]
                                         :compiler {:output-to "target/dev/kioo.js"
                                                    :optimizations :none
                                                    :pretty-print true
                                                    :source-map true}}
                                        {:id "test"
                                         :notify-command ["phantomjs" :cljs.test/runner "target/test/kioo.js"]
                                         :source-paths ["src" "test" "target/classes" "target/test-classes"]
                                         :compiler {:output-to "target/test/kioo.js"
                                                    :optimizations :simple
                                                    :pretty-print true
                                                    :preamble ["phantomjs-shims.js"
                                                               "react/react.js"]
                                                    :externs ["react/externs/react.js"]}}]
                               :test-commands {"phantom" ["phantomjs" :runner "target/test/kioo.js"]}}
                   :repl-options {:nrepl-middleware [cljx.repl-middleware/wrap-cljx]}
                   :resource-paths ["test-resources"]
                   :source-paths ["target/classes"]
                   :test-paths ["test" "target/test-classes"]}})
