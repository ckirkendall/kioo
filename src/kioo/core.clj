(ns kioo.core
  (:refer-clojure :exclude [compile])
  (:require [net.cgrand.enlive-html :refer [at html-resource select
                                            any-node]]))

(declare compile)

(defn attach-transform [trans]
  (fn [node]
    (if (:trans node)
      (assoc node :trans `(fn [node]
                           (cljs.core/-> node
                               (~(:trans node))
                               (~trans))))
      (assoc node :trans trans))))


(defn map-trans [node trans-lst]
  ;(println "//" trans-lst)
  (reduce (fn [node [sel trans]]
            (at node sel (attach-transform trans)))
          node
          trans-lst))

(defmacro component [path & body]
  (let [[sel trans-lst] (if (map? (first body))
                          [[:body :> any-node] (first body)]
                          body)
        root (html-resource path)
        ;_ (println "//" root)
        start (if (= :root sel)
                root
                (select root sel))]
    ;(println "//" start)
    (compile (map-trans start trans-lst))))


(defn compile-attrs [attrs]
  (let [style  (when (:style attrs)
                 (let [vals (re-seq #"\s*([^:;]*)[:][\s]*([^;]+)"
                                    (:style attrs))]
                   (reduce (fn [m [_ k v]]
                             (assoc m k (.trim v)))
                           {} vals)))
        class-name (:class attrs)]
    (-> attrs
        (dissoc :class)
        (merge {:style style :className class-name}))))


(defn get-react-sym [tag]
  (symbol "js" (str "React.DOM." (name tag))))


(defn compile-node [node]
  (let [children (compile (:content node))]
    (if (:trans node)
      `(kioo.core/make-react-dom
        (~(:trans node) ~(-> node
                             (dissoc :trans)
                             (assoc :attrs (compile-attrs (:attrs node))
                                    :content children
                                    :sym (get-react-sym (:tag node))))))
      `(apply ~(get-react-sym (:tag node))
        (cljs.core/clj->js ~(compile-attrs (:attrs node)))
        (kioo.core/flatten-nodes ~children)))))


(defn compile [node]
  (let [nodes (if (map? node) [node] node)
        react-nodes (vec (map #(if (map? %) (compile-node %) %)
                              nodes))]
    `(cljs.core/into-array (kioo.core/flatten-nodes ~react-nodes))))


