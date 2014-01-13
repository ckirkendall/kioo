(ns kioo.core-test
  (:require [cemerick.cljs.test :as t]
            [kioo.core :refer [content set-attr append prepend]]
            [kioo.test :refer [render-dom]]
            [goog.dom :as gdom])
  (:require-macros [kioo.core :refer [component]]
                   [cemerick.cljs.test :refer [are is deftest testing]]))


(deftest render-test
  (testing "basic render test"
    (let [comp (component "simple-div.html" {}) ]
      (is (= "<div>test</div>" (render-dom comp)))))
  (testing "content replace"
    (let [comp (component "simple-div.html"
                          {[:div] (content "success")})]
      (is (= "<div>success</div>" (render-dom comp))))))
