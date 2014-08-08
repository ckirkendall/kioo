(ns kioo.server.core
  (:require [kioo.core :refer [component* snippet*]]
            [kioo.util :refer [convert-attrs flatten-nodes]]
            [net.cgrand.enlive-html :as enlive :refer [any-node]]
            [kioo.common :as common]))

(declare emit-node)

(defn- emit-str
 "Like clojure.core/str but escapes < > and &."
 [x]
 (-> x
     str
     (.replace "&" "&amp;")
     (.replace "<" "&lt;")
     (.replace ">" "&gt;")))

(defn- emit-attr-str
 "Like clojure.core/str but escapes < > & and \"."
 [x]
 (-> x
     str
     (.replace "&" "&amp;")
     (.replace "<" "&lt;")
     (.replace ">" "&gt;")
     (.replace "\"" "&quot;")))

(defn- emit-style-str [smap]
  (reduce (fn [s [k v]] (if (empty? v) s (str (name k) ":" v ";" s)))
          "" smap))


(def self-closing-tags #{:area :base :basefont :br :hr
                         :input :img :link :meta})

(defn attr-by-key [ky]
  (case ky
    :className "class"
    (name ky)))

(defn emit-attrs [attrs]
  (reduce (fn [s [k v]]
            (cond
             (empty? v) s
             (= k :style) (str " style=\""
                               (emit-attr-str (emit-style-str v))
                               "\"")
             :else (str s " " (attr-by-key k) "=\""
                        (emit-attr-str v) "\"")))
          ""
          attrs))


(defn make-dom [node]
  (cond
   (empty? node) ""
   (string? node) node
   (seq? node) (str (make-dom (first node))
                    (make-dom (rest node)))
   (:tag node) (let [{:keys [tag attrs content]} node]
                 (str "<" (name tag) (emit-attrs attrs)
                      (if (self-closing-tags tag)
                        "/>"
                        (str ">" (make-dom content)
                             "</" (name tag) ">"))))
   :else ""))

(defn emit-trans [node children]
  `(make-dom
    (~(:trans node) ~(-> node
                         (assoc :attrs (convert-attrs (:attrs node))
                                :content children)))))

(defn emit-node [{:keys [tag attrs]} children]
  (if (self-closing-tags tag)
    (str "<" (name tag) (emit-attrs attrs) "/>")
    `(str ~(str "<" (name tag) (emit-attrs attrs) ">")
          (apply str ~children)
          ~(str "</" (name tag) ">"))))

(defn wrap-fragment [tag child-sym]
  `(str "<span>" ~child-sym "</span>"))


(def server-emit-opts {:emit-trans emit-trans
                       :emit-node emit-node
                       :wrap-fragment wrap-fragment
                       :emit-str emit-str})


(defmacro component
  "React base component definition"
  ([path trans]
     (component* path trans server-emit-opts))
  ([path sel trans]
     (component* path sel trans server-emit-opts))
  ([path sel trans opts]
     (component* path sel trans (merge server-emit-opts opts))))

(defmacro snippet
  ([path sel args]
     (snippet* path sel {} args server-emit-opts true))
  ([path sel args trans]
     (snippet* path sel trans args server-emit-opts true))
  ([path sel args trans opts]
     (snippet* path sel trans args (merge server-emit-opts opts) true)))

(defmacro template
  ([path args]
     (snippet* path [:body :> any-node] {} args server-emit-opts true))
  ([path args trans]
     (snippet* path [:body :> any-node] trans args server-emit-opts true))
  ([path args trans opts]
     (snippet* path [:body :> any-node] trans args (merge server-emit-opts opts) true)))

(defmacro defsnippet
  ([sym path sel args]
     `(def ~sym ~(snippet* path sel {} args server-emit-opts true)))
  ([sym path sel args trans]
     `(def ~sym ~(snippet* path sel trans args server-emit-opts true)))
  ([sym path sel args trans opts]
     `(def ~sym ~(snippet* path sel trans args (merge server-emit-opts opts) true))))

(defmacro deftemplate
  ([sym path args]
     `(def ~sym ~(snippet* path [:body :> any-node] {} args server-emit-opts true)))
  ([sym path args trans]
     `(def ~sym ~(snippet* path [:body :> any-node] trans args server-emit-opts true)))
  ([sym path args trans opts]
     `(def ~sym ~(snippet* path [:body :> any-node] trans args (merge server-emit-opts opts) true))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TRANSFORMS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def content common/content)
(def append common/append)
(def prepend common/prepend)

(defn after [& body]
  (fn [node]
    (cons node body)))

(defn before [& body]
  (fn [node]
    (flatten-nodes (concat body [node]))))

(def substitute common/substitute)
(def set-attr common/set-attr)
(def remove-attr common/remove-attr)
(def do-> common/do->)
(def set-style common/set-style)
(def remove-style common/remove-style)
(def set-class common/set-class)
(def add-class common/add-class)
(def remove-class common/remove-class)


(defn wrap [tag attrs]
  (fn [node]
    {:tag tag
     :attrs (convert-attrs attrs)
     :content (list node)}))

(def unwrap common/unwrap)
(def html enlive/html)
(def html-content enlive/html-content)
