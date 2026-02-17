(ns test-gen.sequential
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]))

(def x 9)

(def *a (atom 0))

(+ x (swap! *a inc))

;; Express a test by passing a function
;; directly (without a vector):

(kind/test-last pos?)

(+ x (swap! *a inc))

;; Express a test by the
;; `kind/test-last` function:

(kind/test-last [= 11])

(+ x (swap! *a inc))

;; Express a test by the 
;; `^kind/test-last` metadata:

^kind/test-last [= 12]

(+ x (swap! *a inc))

;; Express a test by the 
;; `kindly/check` macro:

(kindly/check = 13)
