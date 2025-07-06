(ns
 test-gen.sequential-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.kindly.v4.api :as kindly]
  [clojure.test :refer [deftest is]]))


(def v1_l5 (def x 9))


(def v2_l7 (def *a (atom 0)))


(def v3_l9 (+ x (swap! *a inc)))


(deftest t5_l14 (is (= v3_l9 10)))


(def v6_l16 (+ x (swap! *a inc)))


(deftest t8_l21 (is (= v6_l16 11)))


(def v9_l23 (+ x (swap! *a inc)))


(deftest t11_l28 (is (= v9_l23 12)))
