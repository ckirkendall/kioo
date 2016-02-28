(ns kioo.core
  (:require [clojure.test :as test :refer :all]
            [clojure.java.io :as io]
            [kioo.core :as core :refer :all]))

(defn path-exists? [p]
  (.exists (io/file p)))

(deftest path-exists-test
  (is (path-exists? "simple-div.html"))
  (is (path-exists? "test-resources/simple-div.html"))
  (is (not (path-exists? "file-does-not-exist.txt")))
  (is (path-exists? :not-a-path)))

