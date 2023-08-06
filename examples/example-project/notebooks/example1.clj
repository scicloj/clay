;; # Clay demo

;; ## Setup

(ns example1
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.kindly.v3.kind :as kind]))

;; ## Examples

(+ 1 2)

(kind/hiccup
 [:div {:style
        {:background-color "#eeddcc"}}
  [:p "hello"]])

(kind/md
 ["hello *hello* **hello**"])
