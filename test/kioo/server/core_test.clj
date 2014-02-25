(ns kioo.server.core-test
  (:use clojure.test)
  (:require [kioo.server.core :refer [component snippet defsnippet
                                      template defsnippet]]))


(deftest render-test
 (testing "basic render test"
    (let [comp (component "simple-div.html" {}) ]
      (is (= "<div id=\"tmp\">test</div>" comp)))))
