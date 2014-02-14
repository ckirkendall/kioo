(ns kioo.util
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [split replace capitalize]]))

#+cljs
(def ^:dynamic *component* nil)

#+cljs
(def WrapComponent
  "Wrapper component used to mix-in lifecycle methods
   This was pulled from quiescent"
  (.createClass js/React
     #js {:render
          (fn []
            (this-as this (aget (.-props this) "wrappee")))
          :componentDidUpdate
          (fn [prev-props prev-state node]
            (this-as this
              (when-let [f (or (aget (.-props this) "onUpdate")
                               (aget (.-props this) "onRender"))]
                (binding [*component* this]
                  (f node)))))
          :componentDidMount
          (fn [node]
            (this-as this
              (when-let [f (or (aget (.-props this) "onMount")
                               (aget (.-props this) "onRender"))]
                (f node))))}))


(def dont-camel-case #{"aria" "data"})

(defn camel-case [dashed]
  (if (string? dashed)
    dashed
    (let [name-str (name dashed)
          [start & parts] (split name-str #"-")]
      (if (dont-camel-case start)
        name-str
        (apply str start (map capitalize parts))))))


(defn convert-attrs [attrs]
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;these functions were pulled from r0man / sablono hiccup style
;;template engine for react.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn attr-pattern
  "Returns a regular expression that matches the HTML attribute `attr`
  and it's value."
  [attr]
  (re-pattern (str "\\s+" (name attr) "\\s*=\\s*['\"][^\"']+['\"]")))

(defn strip-attr
  "Strip the HTML attribute `attr` and it's value from the string `s`."
  [s attr]
  (if s (replace s (attr-pattern attr) "")))
