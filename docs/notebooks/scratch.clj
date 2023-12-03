(ns slides
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.clay.v2.api :as clay]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [tablecloth.api :as tc]))

(kind/hiccup
 [:img {:src "notebooks/images/Clay.svg.png"}])

(kind/hiccup
 [:img {:src "notebooks/images/Kindly.svg"}])

;; # A

;; ## A1

(+ 1 2)

;; ## A2

;; # B
