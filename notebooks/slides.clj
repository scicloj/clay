^{:clay {:quarto {:monofont "Fira Code Medium"}}}
(ns slides
  (:require [scicloj.kindly.v4.kind :as kind]))

;; # Slide 1

(kind/hiccup
 [:img {:src "https://scicloj.github.io/clay/notebooks/images/Clay.svg.png"}])

;; ## Point 1

(+ 1 2)

;; ## Point 2

(str "hello" ", world")

;; # More Slides
