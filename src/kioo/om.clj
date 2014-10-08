(ns kioo.om
  (:require [kioo.core :refer [component* snippet*]]
            [kioo.util :refer [convert-attrs]]
            [net.cgrand.enlive-html :refer [any-node]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; This add better support for om
;; in particular om's wrapping
;; of form fields
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-om-sym [tag]
  (symbol "om.dom" (name tag)))


(defn emit-trans [node children]
  `((kioo.core/handle-wrapper kioo.om/make-dom)
    (~(:trans node) ~(-> node
                         (dissoc :trans)
                         (assoc :attrs (convert-attrs (:attrs node))
                                :content children
                                :sym (get-om-sym (:tag node)))))))

(defn emit-node [node children]
  `(apply ~(get-om-sym (:tag node))
        (cljs.core/clj->js ~(convert-attrs (:attrs node)))
        ~children))


(defn wrap-fragment [tag child-sym]
  `(apply ~(get-om-sym tag) nil ~child-sym))


(def om-emit-opts {:emit-trans emit-trans
                   :emit-node emit-node
                   :wrap-fragment wrap-fragment
                   :resource-wrapper :mini-html})


(defmacro component
  "React base component definition"
  ([path trans]
     (component* path trans om-emit-opts))
  ([path sel trans]
     (component* path sel trans om-emit-opts))
  ([path sel trans opts]
     (component* path sel trans (merge (eval opts) om-emit-opts))))


(defmacro snippet
  ([path sel args]
     (snippet* path sel {} args om-emit-opts))
  ([path sel args trans]
     (snippet* path sel trans args om-emit-opts))
  ([path sel args trans opts]
     (snippet* path sel trans args (merge (eval opts) om-emit-opts))))

(defmacro template
  ([path args]
     (snippet* path [:body :> any-node] {} args om-emit-opts))
  ([path args trans]
     (snippet* path [:body :> any-node] trans args om-emit-opts))
  ([path args trans opts]
     (snippet* path [:body :> any-node] trans args (merge (eval opts) om-emit-opts))))

(defmacro defsnippet
  ([sym path sel args]
     `(def ~sym ~(snippet* path sel {} args om-emit-opts)))
  ([sym path sel args trans]
     `(def ~sym ~(snippet* path sel trans args om-emit-opts)))
  ([sym path sel args trans opts]
     `(def ~sym ~(snippet* path sel trans args (merge (eval opts) om-emit-opts)))))

(defmacro deftemplate
  ([sym path args]
     `(def ~sym ~(snippet* path [:body :> any-node] {} args om-emit-opts)))
  ([sym path args trans]
     `(def ~sym ~(snippet* path [:body :> any-node] trans args om-emit-opts)))
  ([sym path args trans opts]
     `(def ~sym ~(snippet* path [:body :> any-node] trans args (merge (eval opts) om-emit-opts)))))
