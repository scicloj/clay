(ns
 test-gen.simple-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(deftest t3_l9 (is (= (+ 4 5) 9)))


(deftest t6_l16 (is (pos? (+ 4 5))))
