(ns kioo.reagent
  (:require [kioo.core :as core :refer [flatten-nodes]]
            [reagent.impl.template :refer [as-component]]))

(defn make-dom [node & body]
  (let [rnode (if (map? node)
                (let [c (:content node)]
                  (cond
                   (vector? c) [(:tag node)
                                (:attrs node)
                                (as-component c)]
                   (seq? c) (reduce #(conj %1 (as-component %2))
                                    [(:tag node) (:attrs node)]
                                    c)
                   :else [(:tag node) (:attrs node) c])) 
                node)
        rnode (as-component rnode)]
    (if (empty? body)
      rnode
      (cons rnode (make-dom body)))))


(def content core/content)
(def append core/append)
(def prepend core/prepend)

;;after and before need to to make-dom
;;so they are call out specifically
(defn after [& body]
  (fn [node]
    (cons (make-dom node) body)))

(defn before [& body]
  (fn [node]
    (concat body [(make-dom node)])))

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
