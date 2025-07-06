(ns test-gen.sequential
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]))

(def x 9)

(def *a (atom 0))

(+ x (swap! *a inc))

;; Express a test by the
;; `kind/test-last` function:

(kind/test-last [= 10])

(+ x (swap! *a inc))

;; Express a test by the 
;; `^kind/test-last` metadata:

^kind/test-last [= 11]

(+ x (swap! *a inc))

;; Express a test by the 
;; `kindky/check` macro:

(kindly/check = 12)
