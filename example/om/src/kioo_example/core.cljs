(ns kioo-example.core
  (:require [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [kioo.core :refer [handle-wrapper]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [kioo.om :refer [defsnippet deftemplate]]))


(defsnippet my-nav-item "main.html" [:.nav-item]
  [[caption func]]
  {[:a] (do-> (content caption)
              (listen :onClick #(func caption)))})

(defsnippet my-header "main.html" [:header]
  [{:keys [heading navigation]}]
  {[:h1] (content heading)
   [:ul] (content (map my-nav-item navigation))})


(deftemplate my-page "main.html"
  [data]
  {[:header] (substitute (my-header data))
   [:.content] (content (:content data))
   [:div] (set-style :color "red")})

(defn init [data] (om/component (my-page data)))

(def app-state (atom {:heading "main"
                      :content    "Hello World"
                      :navigation [["home" #(js/alert %)]
                                   ["next" #(js/alert %)]]}))

(om/root init app-state {:target  (.-body js/document)})

