^{:kindly/options {:test-mode :simple}}
(ns test-gen.simple
  (:require [scicloj.kindly.v4.kind :as kind]))

;; A simple test:

(+ 4 5)

(kind/test-last
 [= 9])
