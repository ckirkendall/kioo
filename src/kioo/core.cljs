(ns kioo.core
  (:require [kioo.util :refer [convert-attrs WrapComponent *component*
                               camel-case flatten-nodes]]
            [hickory.core :as hic :refer [parse-fragment as-hiccup]]
            [sablono.core :as sab :include-macros true]
            [kioo.common :as common]))


(defn value-component[renderer]
  (let [react-component
        (.createClass js/React
           #js {:shouldComponentUpdate
                (fn [next-props _]
                  (this-as this
                           (not= (aget (.-props this) "value")
                                 (aget next-props "value"))))
                :render
                (fn []
                  (this-as this
                           (binding [*component* this]
                             (apply renderer
                                    (aget (.-props this) "value")
                                    (aget (.-props this) "statics")))))})]
    (fn [value & static-args]
      (react-component #js {:value value :statics static-args}))))


(defn make-dom [node]
  (if (map? node)
      (apply (:sym node)
             (clj->js (:attrs node))
             (flatten-nodes (:content node)))
      node))

(defn to-list [vals]
  (if (seq? vals)
    vals
    (list vals)))

(defn handle-wrapper [dom-fn]
  (fn hw [node & body]
    (let [rnode (cond
                 (seq? node) (apply hw node)
                 (and (map? node) (not (empty? (:events node))))  
                 (let [revents (:events node)]
                   (WrapComponent (clj->js (assoc revents
                                             :wrappee (dom-fn node)))))
                 :else (dom-fn node))]
      (if (empty? body)
        rnode
        (cons rnode (to-list (apply hw body)))))))


(def content common/content)
(def append common/append)
(def prepend common/prepend)

(defn after [& body]
  (fn [node]
    (cons (make-dom node) body)))

(defn before [& body]
  (fn [node]
    (flatten-nodes (concat body [(make-dom node)]))))

(def substitute common/substitute)
(def set-attr common/set-attr)
(def remove-attr common/remove-attr)
(def do-> common/do->)
(def set-style common/set-style)
(def remove-style common/remove-style)
(def set-class common/set-class)
(def add-class common/add-class)
(def remove-class common/remove-class)

(defn wrap [tag attrs]
  (fn [node]
    {:tag tag
     :sym (aget js/React.DOM (name tag)) 
     :attrs (convert-attrs attrs)
     :content [(make-dom node)]}))

(def unwrap common/unwrap)

(defn html [content] (sab/html content))

(defn html-content [content]
  (fn [node]
    (let [children  (map #(-> % (as-hiccup) (sab/html))
                         (parse-fragment content))]
      (assoc node :content children))))

(def react-events #{"onRender" "onUpdate" "onMount"})

(defn listen [& events+fns]
  (let [pairs (map (fn [[k v]] [(camel-case k) v])
                   (partition 2 events+fns))
        [rev sev] (reduce (fn [[r s] [k v]]
                            (if (react-events k)
                              [(assoc r k v) s]
                              [r (assoc s k v)])) [] pairs)]
    (fn [node]
      (assoc node
        :attrs (merge (:attrs node) sev)
        :events (merge (:events node) rev)))))

(defn render [component node]
  (.renderComponent js/React component node))
