(ns kioo.html-parser
  "Sets up parsers to use hickory vs enlive's tag soup we do this because of
   issues in tag soup around <a> tags and block elements. It also optional
   provides minification of the html to avoid excess spans.

   Much of this we pulled form enlive-ws a proof of concept by @jesseshurlock
   It was moved here to because the scope of it increased to include
   non-minified html"
  (:require [net.cgrand.enlive-html :refer [get-resource
                                            register-resource!]]
            [hickory.core :as hickory])

  (:import (com.googlecode.htmlcompressor.compressor HtmlCompressor)
           (java.io StringReader)))


(defn parse
  "Loads and parse using hickory and jsoup to get around issues with enlive's
   parser tag soup.  It parsers an HTML resource and closes the stream."
  [content]

 (hickory/as-hickory (hickory/parse content)))

; Other options here: https://code.google.com/p/htmlcompressor/#Using_HTML_Compressor_from_Java_API
; At this point the only non-default I care about is intertag spaces
; Should be quite easy to add others if they are ever needed or to make it configurable with a second
; param to the type

(defn html-compressor [] (doto (HtmlCompressor.) (.setRemoveIntertagSpaces true)))

(defn whitepace-stripping-parser [stream]
  (let [compressed-html (.compress (html-compressor) (slurp stream))
        parsed-data (parse compressed-html)]
    ;(println compressed-html)
    ;(println parsed-data)
    parsed-data))

(defn standard-parser [stream]
  (parse (slurp stream)))

(deftype MiniHtml [resource])

(deftype StandardHtml [resource])

(defmethod get-resource MiniHtml
  [mh _]
  (let [loader whitepace-stripping-parser
        source (.resource mh)]
    (get-resource source loader)))


(defmethod get-resource StandardHtml
  [mh _]
  (let [loader standard-parser
        source (.resource mh)]
    (get-resource source loader)))


(defmethod register-resource! MiniHtml
  [mh]
  (register-resource! (.resource mh)))


(defmethod register-resource! StandardHtml
  [mh]
  (register-resource! (.resource mh)))
