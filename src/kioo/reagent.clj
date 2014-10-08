(ns kioo.reagent
  (:require [kioo.core :refer [component* get-react-sym
                               emit-node wrap-fragment
                               snippet*]]
            [kioo.util :refer [convert-attrs]]
            [net.cgrand.enlive-html :refer [any-node]]))

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
                        :wrap-fragment wrap-fragment
                        :resource-wrapper :mini-html})


(defmacro component
  "React base component definition"
  ([path trans]
     (component* path trans reagent-emit-opts))
  ([path sel trans]
     (component* path sel trans reagent-emit-opts))
  ([path sel trans opts]
     (component* path sel trans (merge (eval opts) reagent-emit-opts))))

(defmacro snippet
  ([path sel args]
     (snippet* path sel {} args reagent-emit-opts))
  ([path sel args trans]
     (snippet* path sel trans args reagent-emit-opts))
  ([path sel args trans opts]
     (snippet* path sel trans args (merge (eval opts) reagent-emit-opts))))

(defmacro template
  ([path args]
     (snippet* path [:body :> any-node] {} args reagent-emit-opts))
  ([path args trans]
     (snippet* path [:body :> any-node] trans args reagent-emit-opts))
  ([path args trans opts]
     (snippet* path [:body :> any-node] trans args (merge (eval opts) reagent-emit-opts))))

(defmacro defsnippet
  ([sym path sel args]
     `(def ~sym ~(snippet* path sel {} args reagent-emit-opts)))
  ([sym path sel args trans]
     `(def ~sym ~(snippet* path sel trans args reagent-emit-opts)))
  ([sym path sel args trans opts]
     `(def ~sym ~(snippet* path sel trans args (merge (eval opts) reagent-emit-opts)))))

(defmacro deftemplate
  ([sym path args]
     `(def ~sym ~(snippet* path [:body :> any-node] {} args reagent-emit-opts)))
  ([sym path args trans]
     `(def ~sym ~(snippet* path [:body :> any-node] trans args reagent-emit-opts)))
  ([sym path args trans opts]
     `(def ~sym ~(snippet* path [:body :> any-node] trans args (merge (eval opts) reagent-emit-opts)))))
