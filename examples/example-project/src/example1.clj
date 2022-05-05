;; # Clay demo

;; ## Setup

(ns example1
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.clay.v1.tools :as tools]
            [scicloj.clay.v1.extensions :as extensions]
            [scicloj.clay.v1.tool.scittle :as scittle]
            [scicloj.viz.api :as viz]
            [tablecloth.api :as tc]
            [clojisr.v1.r :as r]))

;; ## Useful commands


(def config {:tools [tools/clerk
                     tools/portal
                     tools/scittle]
             :extensions [extensions/clojisr
                          extensions/dataset]})

(clay/start! config)

(comment
  (scittle/show-doc! "src/example1.clj"))

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

(r/r '(rnorm 9))
