(ns kioo.core
  (:require [clojure.test :as test :refer :all]
            [kioo.core :as core :refer :all]))

(deftest path-exists-test
  (is (path-exists? "simple-div.html"))
  (is (path-exists? "test-resources/simple-div.html"))
  (is (not (path-exists? "file-does-not-exist.txt")))
  (is (path-exists? :not-a-path)))

