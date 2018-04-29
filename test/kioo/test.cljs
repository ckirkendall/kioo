(ns kioo.test
  (:require [kioo.util :refer [strip-attr strip-comments]]
            [goog.dom :as dom]
            [react]
            [react-dom]
            [create-react-class]
            [react-dom-factories]))

(defn body []
  (aget (dom/getElementsByTagNameAndClass "body") 0))

(defn render-dom [children]
  (let [container (dom/createDom "div")
        id (gensym)]
    (goog.dom/append (body) container)
    (let [render-fn (fn []
                      (this-as this (react-dom-factories/div
                                      (clj->js {:id id}) children)))
          component (react/createFactory (create-react-class #js {:render render-fn}))]
      (react-dom/render (component) container)
      (let [html (.-innerHTML (goog.dom/getElement (str id)))]
        (goog.dom/removeNode container)
        (-> html
            (strip-attr :data-reactid)
            (strip-attr :data-reactroot)
            (strip-comments))))))
