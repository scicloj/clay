(ns test-gen.sequential-and-simple
  (:require [scicloj.kindly.v4.kind :as kind]))

(def x 9)

;; A sequential test:

(+ x 11)

(kind/test-last [= 20])

;; A simple test:

(+ 4 5)

(kind/test-last
 [= 9]
 {:test-mode :simple})
