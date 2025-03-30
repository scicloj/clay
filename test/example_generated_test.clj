(ns
 example-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [clojure.test :refer [deftest is]]))


(def var3_line8 (defn f [x] (+ x 11)))


(def var4_line11 (range (f 11)))


(def var6_line20 (def d (delay (Thread/sleep 1000) (+ 4 5))))


(def var8_line27 (delay (Thread/sleep 1000) (+ 4 5)))


(def var10_line33 (comment (+ 1 2) (delay (Thread/sleep 1000) (+ 4 5))))


(def var12_line42 (+ 4 5))


(deftest test13_line44 (is (= var12_line42 9)))
