(ns kioo.test
  (:require [kioo.util :refer [strip-attr]]
            [goog.dom :as dom]))

(defn body []
  (aget (dom/getElementsByTagNameAndClass "body") 0))

(defn render-dom [children]
  (let [container (goog.dom/createDom "div")
        id (gensym)]
    (goog.dom/append (body) container)
    (let [render-fn (fn [] (this-as this (js/React.DOM.div
                                         (clj->js {:id id}) children)))
          component (js/React.createClass #js {:render render-fn})]
      (js/React.renderComponent (component) container)
      (let [html (.-innerHTML (goog.dom/getElement (str id)))]
        (goog.dom/removeNode container)
        (strip-attr html :data-reactid)))))
