(ns kioo.reagent
  (:require [kioo.core :as core]
            [kioo.util :as util :refer [flatten-nodes]]
            [reagent.core :refer [as-component]]))

(defn make-dom [node]
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
                node)]
    (as-component rnode)))


(def content core/content)
(def append core/append)
(def prepend core/prepend)

;;after and before need to to make-dom
;;so they are call out specifically
(defn after [& body]
  (fn [node]
    (if (seq? node)
      (concat node body)
      (conj body node))))

(defn before [& body]
  (fn [node]
    (if (seq? node)
      (concat body node)
      (concat body (list node)))))

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
     :attrs attrs
     :content (make-dom node)}))

(def unwrap core/unwrap)
(def html core/html)
(def html-content core/html-content)
(def listen core/listen)
(def lifecycle core/lifecycle)
