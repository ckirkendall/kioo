(ns kioo.server.core-test
  (:use clojure.test)
  (:require [kioo.server.core :refer [component snippet defsnippet
                                      template defsnippet content
                                      content set-attr append prepend
                                      remove-attr before after do->
                                      set-style remove-style add-class
                                      remove-class wrap unwrap set-class
                                      html html-content]]))


(deftest render-test
 (testing "basic render test"
    (let [comp (component "simple-div.html" {}) ]
      (is (= "<div id=\"tmp\">test</div>" comp))))
 
  (testing "content replace"
    (let [comp (component "simple-div.html" 
                          {[:div] (content "success")})]
      (is (= "<div id=\"tmp\">success</div>" comp))))
  (testing "first-of-type naked symbol"
    (let [comp (component "list.html" [:ul [:li first-of-type]] {})]
      (is (= "<li>1</li>" comp))))
  (testing "attr= content replace"
    (let [comp (component "simple-attr-div.html"
                          {[(attr= :data-id "tmp")]
                           (content "success")})]
      (is (= "<div data-id=\"tmp\">success</div>" comp))))
  (testing "attr? content replace"
    (let [comp (component "simple-attr-div.html"
                          {[(attr? :data-id)]
                           (content "success")})]
      (is (= "<div data-id=\"tmp\">success</div>" comp))))
  (testing "append test"
    (let [comp (component "simple-div.html"
                          {[:div] (append "success")})]
      ;;note that ract wraps text nodes in span tags
      ;;this is expected to be corrected soon in react but
      ;;for now this is correct
      (is (= "<div id=\"tmp\">testsuccess</div>"
             comp))))
  (testing "prepend test"
    (let [comp (component "simple-div.html"
                          {[:div] (prepend "success")})]
      (is (= "<div id=\"tmp\">successtest</div>"
             comp))))
  (testing "set-attr test"
    (let [comp (component "simple-div.html"
                          {[:div] (set-attr :id "success")})]
      (is (= "<div id=\"success\">test</div>"
             comp))))
  (testing "remove-attr test"
    (let [comp (component "simple-div.html"
                          {[:div] (remove-attr :id)})]
      (is (= "<div>test</div>"
             comp))))
  (testing "before test"
    (let [comp (component "simple-div.html"
                          {[:div] (before "success")})]
      (is (= "success<div id=\"tmp\">test</div>"
             comp))))
  (testing "after test"
    (let [comp (component "simple-div.html"
                          {[:div] (after "success")})]
      (is (= "<div id=\"tmp\">test</div>success"
             comp))))
  (testing "add-class test"
    (let [comp (component "class-span.html" [:span]
                          {[:#s] (add-class "suc")})]
      (is (= "<span class=\"cl cls suc\" id=\"s\">testing</span>"
             comp))))
  (testing "remove-class test"
    (let [comp (component "class-span.html" [:span]
                          {[:#s] (remove-class "cl")})]
      (is (= "<span class=\" cls\" id=\"s\">testing</span>"
             comp))))
  (testing "set-class test"
    (let [comp (component "class-span.html" [:span]
                          {[:#s] (set-class "cl")})]
      (is (= "<span class=\" cl\" id=\"s\">testing</span>"
             comp))))
  (testing "set-style test"
    (let [comp (component "style-span.html" [:span]
                          {[:#s] (set-style :display "none")})]
      (is (= "<span style=\"color:red;display:none;\" id=\"s\">testing</span>"
             comp))))
  (testing "remove-style test"
    (let [comp (component "style-span.html" [:span]
                          {[:#s] (remove-style :color)})]
      (is (= "<span id=\"s\">testing</span>"
             comp))))
  (testing "do-> test"
    (let [comp (component "style-span.html" [:span]
                          {[:#s] (do->
                                  (remove-attr :id)
                                  (remove-style :color))})]
      (is (= "<span>testing</span>"
             comp))))
  (testing "wrap test"
    (let [comp (component "wrap-test.html" [:span]
                          {[:#s] (wrap :div {:id "test"})})]
      (is (= "<div id=\"test\"><span id=\"s\">testing</span></div>"
             comp))))
  (testing "unwrap test"
    (let [comp (component "wrap-test.html" [:div]
                          {[:div] unwrap})]
      (is (= "<span id=\"s\">testing</span>"
             comp))))
  (testing "html test"
    (let [comp (component "simple-div.html"
                          {[:div] (content (html [:h1 {:class "t"}
                                                  [:span "t1"]]))})]
      (is (= "<div id=\"tmp\"><h1 class=\"t\"><span>t1</span></h1></div>"
             comp))))
  (testing "html-content test"
    (let [comp (component "simple-div.html"
                          {[:div] (html-content "<h1>t1</h1><em><span>t2</span></em>")})]
      (is (= "<div id=\"tmp\"><h1>t1</h1><em><span>t2</span></em></div>"
             comp)))))
