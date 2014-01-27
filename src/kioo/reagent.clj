(ns kioo.reagent
  (:require [kioo.core :refer [component*]]
            [kioo.util :refer [convert-attrs]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; This support for reagent
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn emit-trans [node children]
  `(kioo.reagent/make-dom
    (~(:trans node) ~(-> node
                         (dissoc :trans)
                         (assoc :attrs (convert-attrs (:attrs node))
                                :content children
                                :sym (get-om-sym (:tag node)))))))

(defn emit-node [node children]
  `(do [~(:tag node)
        ~(:attrs node)
        ~@children]))


(defn wrap-fragment [tag child-sym]
  `(do [~tag  nil ~child-sym]))


(def reagent-emit-opts {:emit-trans emit-trans
                        :emit-node emit-node
                        :wrap-fragment wrap-fragment})

(defmacro component
  "reagent base component definition"
  [path & body]
  (component* path body reagent-emit-opts))
