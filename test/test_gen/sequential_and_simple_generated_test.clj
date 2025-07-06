(ns
 test-gen.sequential-and-simple-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(def v1_l4 (def x 9))


(def v3_l8 (+ x 11))


(deftest t4_l10 (is (= v3_l8 20)))


(def v6_l14 (+ 4 5))


(deftest t7_l16 (is (= (+ 4 5) 9)))
