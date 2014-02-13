(ns kioo.reagent
  (:require [kioo.core :refer [component* get-react-sym
                               emit-node wrap-fragment
                               snippet*]]
            [kioo.util :refer [convert-attrs]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; This support for reagent
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn emit-trans [node children]
  `((kioo.core/handle-wrapper kioo.reagent/make-dom)
    (~(:trans node)
     ~(-> node
          (dissoc :trans)
          (assoc :attrs (convert-attrs (:attrs node))
                 :content children)))))



(def reagent-emit-opts {:emit-trans emit-trans
                        :emit-node emit-node
                        :wrap-fragment wrap-fragment})

(defmacro component
  "reagent base component definition"
  [path & body]
  (component* path body reagent-emit-opts))


(defmacro snippet [path sel args & trans]
  (snippet* path (cons sel trans) args reagent-emit-opts))

(defmacro template [path args & trans]
  (snippet* path  trans args reagent-emit-opts))

(defmacro defsnippet [sym path sel args & trans]
  `(def ~sym ~(snippet* path (cons sel trans) args reagent-emit-opts)))

(defmacro deftemplate [sym path args & trans]
  `(def ~sym ~(snippet* path trans args reagent-emit-opts)))
