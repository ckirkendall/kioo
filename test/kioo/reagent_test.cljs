(ns kioo.reagent-test
  (:require [cemerick.cljs.test :as t]
            [kioo.reagent :refer [content set-attr append prepend
                               remove-attr before after do->
                               set-style remove-style add-class
                               remove-class wrap unwrap set-class
                               html html-content]]
            [reagent.core :as reagent :refer [atom]]
            [kioo.util :as util]
            [goog.dom :as gdom])
  (:require-macros [kioo.om :refer [component]]
                   [cemerick.cljs.test :refer [are is deftest testing]]))

;; all text get surrounded by spans in om
;; its a bit ugly but it is the expected
;; behavior
(defn inner-html [node]
  (let [html (.-innerHTML node)]
      (util/strip-attr html :data-reactid)))

(defn initial-render [comp]
  (let [comp-fn #(do comp)
        container (goog.dom/createDom "div")]
    (gdom/append (.-body js/document) container)
    (reagent/render-component [comp] container)
    container))

(defn clean-up [] (gdom/removeChildren (.-body js/document)))

(defn render-dom [comp]
  #_(println (pr-str comp))
  (let [res (-> comp
                (initial-render)
                (inner-html))]
    (clean-up)
    res))

(deftest render-test
  (testing "basic render test"
    (let [comp #(component "simple-div.html" {}) ]
      (is (= "<div id=\"tmp\"><span>test</span></div>"
             (render-dom comp)))))
  (testing "content replace"
    (let [atm (atom "one")
          comp #(component "simple-div.html"  
                           {[:div] (content @atm)})
          container (initial-render comp)
          html-str1 (inner-html container)
          _ (reset! atm "two")
          html-str2 (inner-html container)]
      (is (= "<div id=\"tmp\"><span>one</span></div>" html-str1))
      (is (= "<div id=\"tmp\"><span>two</span></div>" html-str2))
      (clean-up)))
  #_(testing "first-of-type naked symbol"
    (let [comp (component "list.html" [:ul [:li first-of-type]] {})]
      (is (= "<li><span>1</span></li>" (render-dom comp)))))
  #_(testing "attr= content replace"
    (let [comp (component "simple-attr-div.html"
                          {[(attr= :data-id "tmp")]
                           (content "success")})]
      (is (= "<div data-id=\"tmp\"><span>success</span></div>"
             (render-dom comp)))))
  #_(testing "attr? content replace"
    (let [comp (component "simple-attr-div.html"
                          {[(attr? :data-id)]
                           (content "success")})]
      (is (= "<div data-id=\"tmp\"><span>success</span></div>"
             (render-dom comp)))))
  #_(testing "append test"
    (let [comp (component "simple-div.html"
                          {[:div] (append "success")})]
      ;;note that ract wraps text nodes in span tags
      ;;this is expected to be corrected soon in react but
      ;;for now this is correct
      (is (= "<div id=\"tmp\"><span>test</span><span>success</span></div>"
             (render-dom comp)))))
  #_(testing "prepend test"
    (let [comp (component "simple-div.html"
                          {[:div] (prepend "success")})]
      (is (= "<div id=\"tmp\"><span>success</span><span>test</span></div>"
             (render-dom comp)))))
  #_(testing "set-attr test"
    (let [comp (component "simple-div.html"
                          {[:div] (set-attr :id "success")})]
      (is (= "<div id=\"success\"><span>test</span></div>"
             (render-dom comp)))))
  #_(testing "remove-attr test"
    (let [comp (component "simple-div.html"
                          {[:div] (remove-attr :id)})]
      (is (= "<div><span>test</span></div>"
             (render-dom comp)))))
  #_(testing "before test"
    (let [comp (component "simple-div.html"
                          {[:div] (before "success")})]
      (is (= "<span><span>success</span><div id=\"tmp\"><span>test</span></div></span>"
             (render-dom comp)))))
  #_(testing "after test"
    (let [comp (component "simple-div.html"
                          {[:div] (after "success")})]
      (is (= "<span><div id=\"tmp\"><span>test</span></div><span>success</span></span>"
             (render-dom comp)))))
  #_(testing "add-class test"
    (let [comp (component "class-span.html" [:span]
                          {[:#s] (add-class "suc")})]
      (is (= "<span class=\"cl cls suc\" id=\"s\"><span>testing</span></span>"
             (render-dom comp)))))
  #_(testing "remove-class test"
    (let [comp (component "class-span.html" [:span]
                          {[:#s] (remove-class "cl")})]
      (is (= "<span class=\" cls\" id=\"s\"><span>testing</span></span>"
             (render-dom comp)))))
  #_(testing "set-class test"
    (let [comp (component "class-span.html" [:span]
                          {[:#s] (set-class "cl")})]
      (is (= "<span class=\" cl\" id=\"s\"><span>testing</span></span>"
             (render-dom comp)))))
  #_(testing "set-style test"
    (let [comp (component "style-span.html" [:span]
                          {[:#s] (set-style :display "none")})]
      (is (= "<span style=\"color:red;display:none;\" id=\"s\"><span>testing</span></span>"
             (render-dom comp)))))
  #_(testing "remove-style test"
    (let [comp (component "style-span.html" [:span]
                          {[:#s] (remove-style :color)})]
      (is (= "<span id=\"s\"><span>testing</span></span>"
             (render-dom comp)))))
  #_(testing "do-> test"
    (let [comp (component "style-span.html" [:span]
                          {[:#s] (do->
                                  (remove-attr :id)
                                  (remove-style :color))})]
      (is (= "<span><span>testing</span></span>"
             (render-dom comp)))))
  #_(testing "wrap test"
    (let [comp (component "wrap-test.html" [:span]
                          {[:#s] (wrap :div {:id "test"})})]
      (is (= "<div id=\"test\"><span id=\"s\"><span>testing</span></span></div>"
             (render-dom comp)))))
  #_(testing "unwrap test"
    (let [comp (component "wrap-test.html" [:div]
                          {[:div] unwrap})]
      (is (= "<span id=\"s\"><span>testing</span></span>"
             (render-dom comp)))))
  #_(testing "html test"
    (let [comp (component "simple-div.html"
                          {[:div] (content (html [:h1 {:class "t"}
                                                  [:span "t1"]]))})]
      (is (= "<div id=\"tmp\"><h1 class=\"t\"><span>t1</span></h1></div>"
             (render-dom comp)))))
  #_(testing "html-content test"
    (let [comp (component "simple-div.html"
                          {[:div] (html-content "<h1>t1</h1><em><span>t2</span></em>")})]
      (is (= "<div id=\"tmp\"><h1>t1</h1><em><span>t2</span></em></div>"
             (render-dom comp))))))
