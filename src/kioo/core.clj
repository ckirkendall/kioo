(ns kioo.core
  (:refer-clojure :exclude [compile])
  (:require [kioo.util :refer [convert-attrs flatten-nodes]]
            [net.cgrand.enlive-html :refer [at html-resource select
                                            any-node]]
            [clojure.string :as string]))

(declare compile component*)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; BASE REACT EMIT FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-react-sym [tag]
  (symbol "js" (str "React.DOM." (name tag))))


(defn emit-trans [node children]
  `((kioo.core/handle-wrapper kioo.core/make-dom)
    (~(:trans node) ~(-> node
                         (dissoc :trans)
                         (assoc :attrs (convert-attrs (:attrs node))
                                :content children
                                :sym (get-react-sym (:tag node)))))))

(defn emit-node [node children]
  `(apply ~(get-react-sym (:tag node))
          (cljs.core/clj->js ~(convert-attrs (:attrs node)))
          (kioo.util/flatten-nodes ~children)))


(defn wrap-fragment [tag child-sym]
  `(apply ~(get-react-sym tag) nil ~child-sym))


(def react-emit-opts {:emit-trans emit-trans
                      :emit-node emit-node
                      :wrap-fragment wrap-fragment})

(defmacro component
  "React base component definition"
  ([path trans]
     (component* path trans react-emit-opts))
  ([path sel trans]
     (component* path sel trans react-emit-opts))
  ([path sel trans opts]
     (component* path sel trans (merge (eval opts) react-emit-opts))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Using Enlive to do selection and
;; attache base transforms
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn attach-transform [trans]
  (fn [node]
    (if (:trans node)
      (assoc node :trans `(fn [node#]
                           (cljs.core/-> node#
                               (~(:trans node))
                               (~trans))))
      (assoc node :trans trans)))) 


(defn resolve-enlive-var [sym]
  (ns-resolve 'net.cgrand.enlive-html sym))

(defn eval-selector [sel]
  (reduce
   #(conj %1
          (cond
           (list? %2) (apply (resolve-enlive-var (first %2)) (rest %2))
           (or (vector? %2)
               (map? %2)
               (set? %2)) (eval-selector %2)
           (symbol? %2) (resolve-enlive-var %2)
            :else %2))
   (cond
    (vector? sel) []
    (set? sel) #{}
    (map? sel) {}
    :else []) sel))

(defn map-trans [node trans-lst]
  (reduce (fn [node [sel trans]]
            (at node (eval-selector sel) (attach-transform trans)))
          node
          trans-lst))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main Structure of Compiler
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn component*
  "this is the generic component that takes emitter
   options that define how the component is mapped
   to the underlying library (react, om, reagent)"
  ([path trans emit-opts]
     (component* path [:body :> any-node] trans emit-opts))
  ([path sel trans emit-opts]
      (let [root (html-resource path)
            start (if (= :root sel)
                    root
                    (select root (eval-selector sel)))
            child-sym (gensym "ch")]
        (assert (or (empty? trans) (map? trans))
                "Transforms must be a map - Kioo only supports order independent transforms")
        `(let [~child-sym ~(compile (map-trans start trans) emit-opts)]
           (if (= 1 (count ~child-sym))
             (first ~child-sym)
             ~((:wrap-fragment emit-opts) :span child-sym))))))


(defn compile-node
  "Emits the compiled sturcure for a single node & its children"
  [node emit-opts]
  (when (:tag node)
    (let [children (compile (:content node) emit-opts)
          emit (if (:trans node)
                 (:emit-trans emit-opts)
                 (:emit-node emit-opts))]
      (emit node children))))


(defn compile
  "Emits the compiled structure for a list of nodes"
  [node emit-opts]
  (let [nodes (if (map? node) [node] node)
        cnodes (vec (filter identity
                            (map #(cond
                                   (map? %) (compile-node % emit-opts) 
                                   (string? %) (if (:emit-str emit-opts)
                                                 ((:emit-str emit-opts) %)
                                                 %)
                                   :else %)
                                 nodes)))]
    `(kioo.util/flatten-nodes ~cnodes)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Enlive style template & snippet
;;
;; these were created to provide the ability
;; to share templates from between server
;; and client using cljx 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn snippet*
  ([path sel trans args emit-opts]
     (snippet* path sel trans args emit-opts false))
  ([path sel trans args emit-opts check-val?]
     (if check-val?
       `(kioo.core/value-component
         (fn ~args
           ~(component* path sel trans emit-opts)))
       `(fn ~args
          ~(component* path sel trans emit-opts)))))

(defmacro snippet
  ([path sel args]
     (snippet* path sel {} args react-emit-opts true))
  ([path sel args trans]
     (snippet* path sel trans args react-emit-opts true))
  ([path sel args trans opts]
     (snippet* path sel trans args (merge (eval opts) react-emit-opts) true)))

(defmacro template
  ([path args]
     (snippet* path [:body :> any-node] {} args react-emit-opts true))
  ([path args trans]
     (snippet* path [:body :> any-node] trans args react-emit-opts true))
  ([path args trans opts]
     (snippet* path [:body :> any-node] trans args (merge (eval opts) react-emit-opts) true)))

(defmacro defsnippet
  ([sym path sel args]
     `(def ~sym ~(snippet* path sel {} args react-emit-opts true)))
  ([sym path sel args trans]
     `(def ~sym ~(snippet* path sel trans args react-emit-opts true)))
  ([sym path sel args trans opts]
     `(def ~sym ~(snippet* path sel trans args (merge (eval opts) react-emit-opts) true))))

(defmacro deftemplate
  ([sym path args]
     `(def ~sym ~(snippet* path [:body :> any-node] {} args react-emit-opts true)))
  ([sym path args trans]
     `(def ~sym ~(snippet* path [:body :> any-node] trans args react-emit-opts true)))
  ([sym path args trans opts]
     `(def ~sym ~(snippet* path [:body :> any-node] trans args (merge (eval opts) react-emit-opts) true))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; helper utils
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def supress-whitespace {:emit-str #(when-not (empty? (string/trim %)) %)})
