(ns example
  (:require [scicloj.kindly.v4.kind :as kind]))

(defn f [x] (+ x 3))

(f 2)

(kind/test-last [pos?])

(+ 5 4)

(kind/test-last [= 9])
