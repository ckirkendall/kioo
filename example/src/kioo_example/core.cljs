(ns kioo-example.core
  (:require [kioo.core :refer [content set-attr do-> substitute]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [kioo.core :as kioo]))

(defn my-nav-item [[caption func]]
  (kioo/component "main.html" [:.nav-item]
    {[:a] (do-> (content caption)
                (set-attr :onClick func))}))


(defn my-header [heading nav-elms]
  (kioo/component "main.html" [:header]
    {[:h1] (content heading)
     [:ul] (content (map my-nav-item nav-elms))}))

(defn my-page [data]
  (om/component
   (kioo/component "main.html"
      {[:header] (substitute (my-header (:heading data)
                                        (:navigation data)))
       [:.content] (content (:content data))})))

(def app-state (atom {:content    "Hello World"
                      :navigation [["home" #(js/alert %)]
                                   ["next" #(js/alert %)]]}))

(om/root app-state my-page (.-body js/document))

