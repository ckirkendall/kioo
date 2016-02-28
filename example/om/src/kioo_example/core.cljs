(ns kioo-example.core
  (:require [kioo.om :refer [content set-style set-attr do-> substitute listen lifecycle]]
            [kioo.core :refer [handle-wrapper]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [kioo.om :refer [defsnippet deftemplate]]))


(defsnippet my-nav-item "main.html" [:.nav-item]
  [[caption new-content] update-fn]
  {[:a] (listen :onClick #(update-fn new-content))
   [:h1] (content caption)})

(defsnippet my-header "main.html" [:header]
  [{:keys [heading navigation] :as data}]
  {[:h1] (content heading)
   [:ul] (content (map  #(my-nav-item % (fn [content]
                                          (om/update! data [:content] content)))
                        navigation))})


(deftemplate my-page "main.html"
  [data]
  {[:header] (substitute (my-header data))
   [:.content] (do-> 
                (lifecycle {:on-render (fn [this] (js/console.log "I have been udpated"))})
                (content (:content data)))
   [:div] (set-style :color "red")
   [:.what] (set-style :color "blue")})

(defn init [data] (om/component (my-page data)))

(def app-state (atom {:heading "Om Example"
                      :content    "Hello World"
                      :navigation [["home" "Hello World"]
                                   ["next" "Next Page"]]}))

(om/root init app-state {:target  (.-body js/document)})
