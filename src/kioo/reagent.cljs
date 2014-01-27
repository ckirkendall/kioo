(ns kioo.reagent
  (:require [kioo.core :as core :refer [flatten-nodes]]))

(defn make-dom [node & body]
  (let [rnode (if (map? node)
                (reduce #(conj %1 %2)
                        [(:tag node) (:attrs node)]
                        (flatten-nodes (:content node)))
                node)]
    (if (empty? body)
      rnode
      (vec (cons rnode (make-dom body))))))


(def content core/content)
(def append core/append)
(def prepend core/prepend)

;;after and before need to to make-dom
;;so they are call out specifically
(defn after [& body]
  (fn [node]
    (vec (cons (make-dom node) body))))

(defn before [& body]
  (fn [node]
    (vec (flatten-nodes (concat body [(make-dom node)])))))

(def substitute core/substitute)
(def set-attr core/set-attr)
(def remove-attr core/remove-attr)
(def do-> core/do->)
(def set-style core/set-style)
(def remove-style core/remove-style)
(def set-class core/set-class)
(def add-class core/add-class)
(def remove-class core/remove-class)
(def wrap core/wrap)
(def unwrap core/unwrap)
(def html core/html)
(def html-content core/html-content)
