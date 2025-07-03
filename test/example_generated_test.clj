(ns
 example-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(def var1_line4 (defn f [x] (+ x 3)))


(def var2_line6 (f 2))


(deftest test3_line8 (is (pos? var2_line6)))


(def var4_line10 (+ 5 4))


(deftest test5_line12 (is (= var4_line10 9)))
