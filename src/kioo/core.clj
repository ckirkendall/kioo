(ns kioo.core
  (:refer-clojure :exclude [compile])
  (:require
   [kioo.util :refer [convert-attrs flatten-nodes]]
   [net.cgrand.enlive-html :refer [at html-resource select
                                   any-node]]
   [clojure.string :as string]
   [kioo.html-parser :as parser]
   [hickory.core :as hickory]
   [clojure.java.io :as io]))

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


(def resource-wrapper-fns {:html parser/->StandardHtml
                           :mini-html parser/->MiniHtml})


(def react-emit-opts {:emit-trans emit-trans
                      :emit-node emit-node
                      :wrap-fragment wrap-fragment
                      :single false
                      :resource-wrapper :mini-html})

(defmacro component
  "React base component definition"
  ([path trans]
     (component* path trans react-emit-opts))
  ([path sel trans]
     (component* path sel trans react-emit-opts))
  ([path sel trans opts]
     (component* path sel trans (merge react-emit-opts (eval opts)))))


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
    (fn [sel-acc sel-frag]
      (conj sel-acc
          (cond
           (list? sel-frag) (apply (resolve-enlive-var (first sel-frag)) (eval-selector (rest sel-frag)))
           (or (vector? sel-frag)
               (map? sel-frag)
               (set? sel-frag)) (eval-selector sel-frag)
           (symbol? sel-frag) (resolve-enlive-var sel-frag)
            :else sel-frag)))
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


(defn resolve-resource-fn [resource {wrapper :resource-wrapper}]
  (cond
   (not (string? resource)) identity
   (keyword? wrapper) (resource-wrapper-fns wrapper)
   (symbol? wrapper) (resolve wrapper)
   :else parser/->MiniHtml))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main Structure of Compiler
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn parse-html
  "parses html with a pre-processing step provided by resource-fn
  returns an enlive compatible data structure. If provided, ast-fn
  is applied to the parsed data structure."
  ([path resource-fn]
   (html-resource (resource-fn path)))
  ([path resource-fn ast-fn]
   (let [ast-fn (or ast-fn identity)]
     (ast-fn (parse-html path resource-fn)))))

(defn component*
  "this is the generic component that takes emitter
   options that define how the component is mapped
   to the underlying library (react, om, reagent)"
  ([path trans emit-opts]
     (component* path [:body :> any-node] trans emit-opts))
  ([path sel trans emit-opts]
     (let [path-obj (eval path)
           single? (:single emit-opts)
           emit-opts (dissoc emit-opts :single)
           resource-fn (resolve-resource-fn path-obj emit-opts)
           ast-fn (:process-ast emit-opts)
           root (parse-html path-obj resource-fn ast-fn)
           start-matches (if (= :root sel)
                           root
                           (select root (eval-selector sel)))
           start (if single?
                   (take 1 start-matches)
                   start-matches)
           child-sym (gensym "ch")]
        (assert (or (empty? trans) (map? trans))
                "Transforms must be a map - Kioo only supports order independent transforms")
        (doseq [trans-selector (keys trans)]
          (if (empty? (select start (eval-selector trans-selector)))
            (let [message (format "WARNING: File %s does not contain selector %s %s."
                                  path sel trans-selector)]
              (try
                (throw (AssertionError. message))
                (catch AssertionError e
                  (binding [*out* *err*]
                    (println message)))))))

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
     (snippet* path sel trans args (merge react-emit-opts (eval opts)) true)))

(defmacro template
  ([path args]
     (snippet* path [:body :> any-node] {} args react-emit-opts true))
  ([path args trans]
     (snippet* path [:body :> any-node] trans args react-emit-opts true))
  ([path args trans opts]
     (snippet* path [:body :> any-node] trans args (merge react-emit-opts (eval opts)) true)))

(defmacro defsnippet
  ([sym path sel args]
     `(def ~sym ~(snippet* path sel {} args react-emit-opts true)))
  ([sym path sel args trans]
     `(def ~sym ~(snippet* path sel trans args react-emit-opts true)))
  ([sym path sel args trans opts]
     `(def ~sym ~(snippet* path sel trans args (merge react-emit-opts (eval opts)) true))))

(defmacro deftemplate
  ([sym path args]
     `(def ~sym ~(snippet* path [:body :> any-node] {} args react-emit-opts true)))
  ([sym path args trans]
     `(def ~sym ~(snippet* path [:body :> any-node] trans args react-emit-opts true)))
  ([sym path args trans opts]
     `(def ~sym ~(snippet* path [:body :> any-node] trans args (merge react-emit-opts (eval opts)) true))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; helper utils
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;supress-whitespace is no longer needed anymore because of the use of enlive-ws
(def supress-whitespace {:resource-wrapper :mini-html})
(def include-whitespace {:resource-wrapper :html})
