

;; # A notebook

(ns detailed-ns
  (:require [clojure.core]))

;; ## Intro

;; Let us write a function that adds 9 to numbers.
;; We will call it `abcd`.

(defn abcd [x]
  (+ x
     9))

(abcd 9)

;; ## More examples

;; Form metadata

^:kind/hiccup
[:div
  [:p "hello"]]

;; A symbol

a-symbol

;; Comments using #_ should be ignored:

#_(+ 1 2)

#_#_ (+ 1 2) (+ 3 4)
