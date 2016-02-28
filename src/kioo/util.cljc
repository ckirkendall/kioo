(ns kioo.util
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [split replace capitalize]]
            #?(:cljs [cljsjs.react])))

#?(:cljs
    (do
      (def ^:dynamic *component* nil)

      (def WrapComponent
        "Wrapper component used to mix-in lifecycle methods
        This was modified from a similar setup in quiescent"
        (.createFactory js/React
                        (.createClass js/React
                                      #js {:render
                                           (fn []
                                             (this-as this
                                                      (let [dom-fn (aget (.-props this) "dom-fn")
                                                            node (aget (.-props this) "node")]
                                                        (dom-fn node))))
                                           :getInitialState
                                           (fn []
                                             (this-as this
                                                      (when-let [f  (aget (.-props this) "initState")]
                                                        (binding [*component* this]
                                                          (f this)))))
                                           #_:getDefaultProps
                                           #_(fn []
                                               (this-as this
                                                        (when-let [f (aget (.-props this) "defaultProps")]
                                                          (binding [*component* this]
                                                            (f this)))))
                                           :shouldComponentUpdate
                                           (fn [next-props next-state]
                                             (this-as this
                                                      (if-let [f (aget (.-props this) "shouldUpdate")]
                                                        (binding [*component* this]
                                                          (f this next-props next-state))
                                                        (not= (aget (.-props this) "node")
                                                              (aget next-props "node")))))
                                           :componentWillReceiveProps
                                           (fn [next-props]
                                             (this-as this
                                                      (when-let [f (or (aget (.-props this) "willReceiveProps")
                                                                       (aget (.-props this) "onWillReceiveProps"))]
                                                        (binding [*component* this]
                                                          (f this next-props)))))
                                           :componentWillUpdate
                                           (fn [next-props next-state]
                                             (this-as this
                                                      (when-let [f (or (aget (.-props this) "willUpdate")
                                                                       (aget (.-props this) "onWillUpdate"))]
                                                        (binding [*component* this]
                                                          (f this next-props next-state)))))
                                           :componentDidUpdate
                                           (fn [prev-props prev-state]
                                             (this-as this
                                                      (when-let [f (or (aget (.-props this) "didUpdate")
                                                                       (aget (.-props this) "onUpdate")
                                                                       (aget (.-props this) "onRender"))]
                                                        (binding [*component* this]
                                                          (f this prev-props prev-state)))))
                                           :componentWillMount
                                           (fn []
                                             (this-as this
                                                      (when-let [f (or (aget (.-props this) "willMount")
                                                                       (aget (.-props this) "onWillMount"))]
                                                        (binding [*component* this]
                                                          (f this)))))
                                           :componentDidMount
                                           (fn []
                                             (this-as this
                                                      (when-let [f (or (aget (.-props this) "didMount")
                                                                       (aget (.-props this) "onMount")
                                                                       (aget (.-props this) "onRender"))]
                                                        (binding [*component* this]
                                                          (f this)))))
                                           :componentWillUnmount
                                           (fn []
                                             (this-as this
                                                      (when-let [f (or (aget (.-props this) "willUnmount")
                                                                       (aget (.-props this) "onUnmount"))]
                                                        (binding [*component* this]
                                                          (f this)))))})))))

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
               :formEncType :formNoValidate :frameBorder :httpEquiv :itemProp
               :itemScope :itemType :maxLength :noValidate :radioGroup :readOnly
               :rowSpan :scrollLeft :scrollTop :spellCheck :srcDoc :tabIndex
               :gradientTransform :gradientUnits :spreadMethod :stopColor
               :stopOpacity :strokeLinecap :strokeWidth :textAnchor :viewBox])
    :accept-charset :acceptCharset
    :class          :className
    :for            :htmlFor))

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
