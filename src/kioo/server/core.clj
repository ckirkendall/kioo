(ns kioo.server.core
  (:require [kioo.core :refer [component* snippet*]]
            [kioo.util :refer [convert-attrs]]))

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


(def self-closing-tags #{:area :base :basefont :br :hr
                         :input :img :link :meta})


(defn emit-attrs [attrs]
  (reduce (fn [s [k v]]
            (str s " " (name k) "=\"" (emit-attr-str v) "\""))
          ""
          attrs))


(defn make-dom [node]
  (println node)
  (cond
   (and (seq? node) (empty? node)) ""
   (seq? node) (str (make-dom (first node))
                    (make-dom (rest node)))
   :else (let [{:keys [tag attrs content]} node]
           (str "<" (name tag) (emit-attrs attrs)
                (if (self-closing-tags tag)
                  "/>"
                  (str ">" (make-dom content) "</" (name tag ">")))))))

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
  [path & body]
  (component* path body server-emit-opts))


(defmacro snippet [path sel args & trans]
  (snippet* path (cons sel trans) args server-emit-opts))

(defmacro template [path args & trans]
  (snippet* path  trans args server-emit-opts))

(defmacro defsnippet [sym path sel args & trans]
  `(def ~sym ~(snippet* path (cons sel trans) args server-emit-opts)))

(defmacro deftemplate [sym path args & trans]
  `(def ~sym ~(snippet* path trans args server-emit-opts)))
