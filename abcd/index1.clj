(ns index1
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]))

(+ 1 2)

(kind/test-last [> 0])

(+ 3 9)

(kindly/check > 10)

^kind/test-last
[> 10]
