(ns kioo.util
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [trim split replace capitalize]]))

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


(def attribute-map
  (assoc 
      (reduce #(assoc %1 (keyword (.toLowerCase (name %2))) %2) {}
              [:accessKey :allowFullScreen :allowTransparency :autoComplete
               :autoFocus :autoPlay :cellPadding :cellSpacing :charSet
               :colSpan :contentEditable :contextMenu :dateTime :encType
               :formNoValidate :frameBorder :htmlFor :httpEquiv :maxLength
               :noValidate :radioGroup :readOnly :rowSpan :scrollLeft :scrollTop
               :spellCheck :srcDoc :tabIndex :gradientTransform :gradientUnits
               :spreadMethod :stopColor :stopOpacity :strokeLinecap :strokeWidth
               :textAnchor :viewBox])
    :class
    :className))

(defn transform-keys [attrs]
  (reduce (fn [m [k v]]
            (assoc m (or (attribute-map k) k) v)) {} attrs))

(defn convert-attrs [attrs]
  (let [style  (when (:style attrs)
                 (let [vals (re-seq #"\s*([^:;]*)[:][\s]*([^;]+)"
                                    (:style attrs))]
                   (reduce (fn [m [_ k v]]
                             (assoc m k (.trim v)))
                           {} vals)))
        class-name (:class attrs)]
    (-> attrs
        (transform-keys)
        (assoc :style style))))


(defn flatten-nodes [nodes]
  (reduce #(if (seq? %2)
             (concat %2 %1)
             (conj %1 %2))
          '()
          (reverse nodes)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Clean enlive output to reduce the amount
;; of rampant span tags that om produces
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
#+clj
(defmulti  clean-root type)
#+clj
(defmethod clean-root java.util.Map [c]
  (if (:content c)
    (assoc c :content (clean-root (:content c)))
    c))
#+clj
(defmethod clean-root nil [s]
  false)
#+clj
(defmethod clean-root clojure.lang.LazySeq [c]
  (remove false? (map clean-root c)))
#+clj
(defmethod clean-root java.lang.String [s]
  (if (empty? (re-find #"(^\s*$)" s))
    (clojure.string/trim s)
    false))

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
