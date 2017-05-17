(defproject kioo-example "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [kioo "0.4.2" :eclusions [reagent]]
                 [reagent "0.6.0"]]

  :plugins [[lein-cljsbuild "1.1.6"]]

  :source-paths ["src"]
  :resource-paths ["resources"]
  
  :cljsbuild { 
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {
                :output-to "app.js"
                :pretty-print true         
                :optimizations :simple
                :preamble ["react/react.js"]
                :externs ["react/externs/react.js"]}}]})
