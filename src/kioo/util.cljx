(ns kioo.util
  (:require [clojure.string :refer [replace]]))

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
