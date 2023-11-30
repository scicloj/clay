^{:clay {:quarto {:monofont "Fira Code Medium"}}}
(ns slides
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.clay.v2.api :as clay]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [tablecloth.api :as tc]))

(kind/hiccup
 [:img {:src "notebooks/images/Clay.svg.png"}])

;; # Slides

;; ## A1

(+ 1 2)

;; ## A2

;; # More Slides
