(ns kioo.om
  (:require
    [kioo.core :as core]
    [kioo.util :refer [flatten-nodes convert-attrs]]
    [react-dom-factories]))

(defn make-dom [node & body]
  (if (map? node)
    (apply (:sym node)
           (clj->js (:attrs node))
           (flatten-nodes (:content node)))
    node))


(def content core/content)
(def append core/append)
(def prepend core/prepend)

(defn after [& body]
  (fn [node]
    (if (seq? node)
      (concat node body)
      (cons (make-dom node) body))))

(defn before [& body]
  (fn [node]
    (if (seq? node)
      (concat body node)
      (concat body [(make-dom node)]))))

(def substitute core/substitute)
(def set-attr core/set-attr)
(def remove-attr core/remove-attr)
(def do-> core/do->)
(def set-style core/set-style)
(def remove-style core/remove-style)
(def set-class core/set-class)
(def add-class core/add-class)
(def remove-class core/remove-class)

(defn wrap [tag attrs]
  (fn [node]
    {:tag tag
     :sym (aget react-dom-factories (name tag))
     :attrs (convert-attrs attrs)
     :content [(make-dom node)]}))

(def unwrap core/unwrap)
(def html core/html)
(def html-content core/html-content)
(def listen core/listen)
(def lifecycle core/lifecycle)
