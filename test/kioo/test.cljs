(ns kioo.test
  (:require [kioo.util :refer [strip-attr strip-comments]]
            [goog.dom :as dom]))

(defn body []
  (aget (dom/getElementsByTagNameAndClass "body") 0))

(defn render-dom [children]
  (let [container (goog.dom/createDom "div")
        id (gensym)]
    (goog.dom/append (body) container)
    (let [render-fn (fn []
                      (this-as this (js/React.DOM.div
                                     (clj->js {:id id}) children)))
          component (js/React.createFactory (js/React.createClass #js {:render render-fn}))]
      (js/ReactDOM.render (component) container)
      (let [html (.-innerHTML (goog.dom/getElement (str id)))]
        (goog.dom/removeNode container)
        (-> html
            (strip-attr :data-reactid)
            (strip-attr :data-reactroot)
            (strip-comments))))))
