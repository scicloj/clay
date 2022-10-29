(ns user
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.kindly-default.v1.api :as kindly-default]))

;; Initialize Kindly's [default](https://github.com/scicloj/kindly-default).
(kindly-default/setup!)

;; Start Clay.
(clay/start!)
