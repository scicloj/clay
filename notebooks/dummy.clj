(ns dummy
  (:require [scicloj.kindly.v4.kind :as kind]))

(defn random-data [n]
  (->> (repeatedly n #(- (rand) 0.5))
       (reductions +)
       (map-indexed (fn [x y]
                      {:w (rand-int 9)
                       :z (rand-int 9)
                       :x x
                       :y y}))))

(random-data 4)

(defn vega-lite-point-plot [data]
  (kind/vega-lite
   {:data {:values data},
    :mark "point"
    :encoding
    {:size {:field "w" :type "quantitative"}
     :x {:field "x", :type "quantitative"},
     :y {:field "y", :type "quantitative"},
     :fill {:field "z", :type "nominal"}}}))

(vega-lite-point-plot (random-data 9))

(comment
  (require '[scicloj.clay.v2.api :as clay])
  (clay/make-hiccup {:source-path "notebooks/dummy.clj"
                     :inline-js-and-css true}))
