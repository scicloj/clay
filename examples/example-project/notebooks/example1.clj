;; # Clay demo

;; ## Setup

(ns example1
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.viz.api :as viz]
            [tablecloth.api :as tc]))

;; ## Useful commands

^:kindly/hide-code?
(comment
  (do (clay/show-doc! "notebooks/example1.clj"
                      {:toc? true})
      (clay/write-html! "docs/example1.html")))

;; ## Examples

(+ 1 2)

(-> (let [n 99]
      (tc/dataset {:preferred-language (for [i (range n)]
                                         (["clojure" "clojurescript" "babashka"]
                                          (rand-int 3)))
                   :age (for [i (range n)]
                          (rand-int 100))})))

(-> [{:x 1 :y 2}
     {:x 2 :y 4}
     {:x 3 :y 9}]
    viz/data
    (viz/type :point)
    (viz/mark-size 200)
    (viz/color :x)
    viz/viz)
