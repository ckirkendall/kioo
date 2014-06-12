(ns kioo.core
  (:refer-clojure :exclude [compile])
  (:require [kioo.util :refer [convert-attrs flatten-nodes clean-root]]
            [net.cgrand.enlive-html :refer [at html-resource select
                                            any-node]]))

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
  [path & body]
  (component* path body react-emit-opts))


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
  [path body emit-opts]
  (let [[sel trans-lst] (if (map? (first body))
                          [[:body :> any-node] (first body)]
                          body)
        sel (or sel [:body :> any-node])
        root (clean-root (html-resource path))
        start (if (= :root sel)
                root
                (select root (eval-selector sel)))
        child-sym (gensym "ch")]
    (assert (or (empty? trans-lst) (map? trans-lst))
            "Transforms must be a map - Kioo only supports order independent transforms")
    `(let [~child-sym ~(compile (map-trans start trans-lst) emit-opts)]
       (if (= 1 (count ~child-sym))
         (first ~child-sym)
         ~((:wrap-fragment emit-opts) :span child-sym)))))


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
        cnodes (vec (map #(cond
                           (map? %) (compile-node % emit-opts) 
                           (string? %) (if (:emit-str emit-opts)
                                         ((:emit-str emit-opts) %)
                                         %)
                           :else %)
                         nodes))]
    `(kioo.util/flatten-nodes ~cnodes)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Enlive style template & snippet
;;
;; these were created to provide the ability
;; to share templates from between server
;; and client using cljx
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn snippet*
  ([path body args emit-opts]
     (snippet* path body args emit-opts false))
  ([path body args emit-opts check-val?]
     (if check-val?
       `(kioo.core/value-component
         (fn ~args
           ~(component* path body emit-opts)))
       `(fn ~args
          ~(component* path body emit-opts)))))

(defmacro snippet [path sel args & trans]
  (snippet* path (cons sel trans) args react-emit-opts true))

(defmacro template [path args & trans]
  (snippet* path trans args react-emit-opts true))

(defmacro defsnippet [sym path sel args & trans]
  `(def ~sym
     ~(snippet* path (cons sel trans) args react-emit-opts true)))

(defmacro deftemplate [sym path args & trans]
  `(def ~sym ~(snippet* path trans args react-emit-opts true)))

