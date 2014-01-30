(ns kioo-example.core
  (:require [kioo.reagent :refer [content set-attr do-> substitute]]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [kioo.reagent :as kioo]))

(declare data nav)

(defn my-nav-item [[caption func]]
  (kioo/component "main.html" [:.nav-item]
    {[:a] (do-> (content caption)
                (set-attr :on-click func))}))


(defn my-header []
  (kioo/component "main.html" [:header]
    {[:h1] (content (:header @data))
     [:ul] (content (map my-nav-item (:navigation @nav)))}))

(defn my-page []
  (kioo/component "main.html"
     {[:header] (substitute [my-header])
      [:.content] (content (:content @data))}))

(def data (atom {:header "main"
                 :content "Hello World"}))
(def nav (atom {:navigation [["home" #(swap! data
                                             assoc :content "home")]
                             ["next" #(swap! data
                                             assoc :content "next")]]}))

(reagent/render-component [my-page] (.-body js/document))

