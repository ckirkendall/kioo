(ns kioo.om-test
  (:require [cljs.test :as t]
            [kioo.om :refer [content set-attr append prepend
                             remove-attr before after do->
                             set-style remove-style add-class
                             remove-class wrap unwrap set-class
                             html html-content listen set-attr]]
            [kioo.test :refer [render-dom]]
            [goog.dom :as gdom])
  (:require-macros [kioo.om :refer [component snippet template
                                    defsnippet deftemplate]]
                   [cljs.test :refer [are is deftest testing]]))

;; all text get surrounded by spans in om
;; its a bit ugly but it is the expected
;; behavior

(deftest render-test
  (testing "basic render test"
    (let [comp (component "simple-div.html" {}) ]
      (is (= "<div id=\"tmp\">test</div>"
             (render-dom comp)))))
  (testing "content replace"
    (let [comp (component "simple-div.html"
                          {[:div] (content "success")})]
      (is (= "<div id=\"tmp\">success</div>"
             (render-dom comp)))))
  (testing "first-of-type naked symbol"
    (let [comp (component "list.html" [:ul [:li first-of-type]] {})]
      (is (= "<li>1</li>" (render-dom comp)))))
  (testing "attr= content replace"
    (let [comp (component "simple-attr-div.html"
                          {[(attr= :data-id "tmp")]
                           (content "success")})]
      (is (= "<div data-id=\"tmp\">success</div>"
             (render-dom comp)))))
  (testing "attr? content replace"
    (let [comp (component "simple-attr-div.html"
                          {[(attr? :data-id)]
                           (content "success")})]
      (is (= "<div data-id=\"tmp\">success</div>"
             (render-dom comp)))))
  (testing "append test"
    (let [comp (component "simple-div.html"
                          {[:div] (append "success")})]
      ;;note that ract wraps text nodes in span tags
      ;;this is expected to be corrected soon in react but
      ;;for now this is correct
      (is (= "<div id=\"tmp\"><span>test</span><span>success</span></div>"
             (render-dom comp)))))
  (testing "prepend test"
    (let [comp (component "simple-div.html"
                          {[:div] (prepend "success")})]
      (is (= "<div id=\"tmp\"><span>success</span><span>test</span></div>"
             (render-dom comp)))))
  (testing "set-attr test"
    (let [comp (component "simple-div.html"
                          {[:div] (set-attr :id "success")})]
      (is (= "<div id=\"success\">test</div>"
             (render-dom comp)))))
  (testing "remove-attr test"
    (let [comp (component "simple-div.html"
                          {[:div] (remove-attr :id)})]
      (is (= "<div>test</div>"
             (render-dom comp)))))
  (testing "before test"
    (let [comp (component "simple-div.html"
                          {[:div] (before "success")})]
      (is (= "<span><span>success</span><div id=\"tmp\">test</div></span>"
             (render-dom comp)))))
  (testing "after test"
    (let [comp (component "simple-div.html"
                          {[:div] (after "success")})]
      (is (= "<span><div id=\"tmp\">test</div><span>success</span></span>"
             (render-dom comp)))))
  (testing "add-class test"
    (let [comp (component "class-span.html" [:span]
                          {[:#s] (add-class "suc")})]
      (is (= "<span id=\"s\" class=\"cl cls suc\">testing</span>"
             (render-dom comp)))))
  (testing "remove-class test"
    (let [comp (component "class-span.html" [:span]
                          {[:#s] (remove-class "cl")})]
      (is (= "<span id=\"s\" class=\" cls\">testing</span>"
             (render-dom comp)))))
  (testing "set-class test"
    (let [comp (component "class-span.html" [:span]
                          {[:#s] (set-class "cl")})]
      (is (= "<span id=\"s\" class=\" cl\">testing</span>"
             (render-dom comp)))))
  (testing "set-style test"
    (let [comp (component "style-span.html" [:span]
                          {[:#s] (set-style :display "none")})]
      (is (= "<span id=\"s\" style=\"color:red;display:none;\">testing</span>"
             (render-dom comp)))))
  (testing "remove-style test"
    (let [comp (component "style-span.html" [:span]
                          {[:#s] (remove-style :color)})]
      (is (= "<span id=\"s\">testing</span>"
             (render-dom comp)))))
  (testing "do-> test"
    (let [comp (component "style-span.html" [:span]
                          {[:#s] (do->
                                  (remove-attr :id)
                                  (remove-style :color))})]
      (is (= "<span>testing</span>"
             (render-dom comp)))))
  (testing "wrap test"
    (let [comp (component "wrap-test.html" [:span]
                          {[:#s] (wrap :div {:id "test"})})]
      (is (= "<div id=\"test\"><span id=\"s\">testing</span></div>"
             (render-dom comp)))))
  (testing "unwrap test"
    (let [comp (component "wrap-test.html" [:div]
                          {[:div] unwrap})]
      (is (= "<span id=\"s\">testing</span>"
             (render-dom comp)))))
  (testing "html test"
    (let [comp (component "simple-div.html"
                          {[:div] (content (html [:h1 {:class "t"}
                                                  [:span "t1"]]))})]
      (is (= "<div id=\"tmp\"><h1 class=\"t\"><span>t1</span></h1></div>"
             (render-dom comp)))))
  (testing "html-content test"
    (let [comp (component "simple-div.html"
                          {[:div] (html-content "<h1>t1</h1><em><span>t2</span></em>")})]
      (is (= "<div id=\"tmp\"><h1>t1</h1><em><span>t2</span></em></div>"
             (render-dom comp)))))
  (testing "listen on render"
    (let [atm (atom "fail")
          comp (component "simple-div.html"
                     {[:div] (listen :on-render
                                     #(reset! atm "success"))})]
      (render-dom comp)
      (is (= "success" @atm)))))




(defsnippet snip1 "wrap-test.html" [:span] [] {})
(deftemplate tmp1 "simple-div.html" [] {})
(defsnippet snip2 "wrap-test.html" [:span] [val]
  {[:span] (content val)})
(deftemplate tmp2 "simple-div.html" [val]
  {[:div] (content val)})

(deftest snippet_template_test
  (testing "basic setup for snippet"
    (let [comp (snippet "wrap-test.html" [:span] [] {})]
      (is (= "<span id=\"s\">testing</span>"
             (render-dom (comp))))))
  (testing "basic setup for template"
    (let [comp (template "simple-div.html" [] {})]
      (is (= "<div id=\"tmp\">test</div>"
             (render-dom (comp))))))
  (testing "simple tranform for snippet"
    (let [comp (snippet "wrap-test.html" [:span] [val]
                        {[:span] (content val)})]
      (is (= "<span id=\"s\">test</span>"
             (render-dom (comp "test"))))))
  (testing "simple transform for template"
    (let [comp (template "simple-div.html" [val]
                         {[:div] (content val)})]
      (is (= "<div id=\"tmp\">success</div>"
             (render-dom (comp "success"))))))
  (testing "basic setup for defsnippet"
    (is (= "<span id=\"s\">testing</span>"
           (render-dom (snip1)))))
  (testing "basic setup for deftemplate"
    (is (= "<div id=\"tmp\">test</div>"
           (render-dom (tmp1)))))
  (testing "simple transform for defsnippet"
    (is (= "<span id=\"s\">test</span>"
           (render-dom (snip2 "test")))))
  (testing "simple transform for deftemplate"
    (is (= "<div id=\"tmp\">success</div>"
           (render-dom (tmp2 "success"))))))


(deftest ordering-inside-do->test
  (testing "Testing content, after then before"
    (let [comp (component "simple-div.html" {[:div]
                                             (do-> (content "success")
                                                   (after "after")
                                                   (before "before"))}) ]
      (is (= "<span><span>before</span><div id=\"tmp\">success</div><span>after</span></span>"
             (render-dom comp)))))
  (testing "Testing content, before then after"
    (let [comp (component "simple-div.html" {[:div]
                                             (do-> (content "success")
                                                   (before "before")
                                                   (after "after"))}) ]
      (is (= "<span><span>before</span><div id=\"tmp\">success</div><span>after</span></span>"
             (render-dom comp))))))

(deftemplate nested-has-template "nested-has.html" []
             {[[:.form-group (has [[:input (attr= :name "name")]])]] (set-attr :id "test")})

(deftest nested-has-test
         (testing "nested has selector"
                  (is (= (render-dom (nested-has-template))
                         "<div class=\"form-group\" id=\"test\"><input type=\"text\" name=\"name\"></div>"))))

;; kioo-generated component takes a long time to appear in the page when using reagent but not Om
(deftemplate minform "min-form.html" [])

(deftest form-timing-test
         (testing "Om-Kioo doesn't suffer from slow construction of React nodes in Safari/Phantom"
                  (is (= (render-dom minform) ""))))
