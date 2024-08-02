(ns index2
  (:require [scicloj.kindly.v4.kind :as kind]))

(defn random-data [n]
  (->> (repeatedly n #(- (rand) 0.5))
       (reductions +)
       (map-indexed (fn [x y]
                      {:w (rand-int 9)
                       :z (rand-int 9)
                       :x x
                       :y y}))))

(defn random-vega-lite-plot [n]
  (-> n
      random-data
      vega-lite-point-plot))

(random-vega-lite-plot 9)

(kind/vega-lite
 {:data {:values kkdata},
  :mark "point"
  :encoding
  {:size {:field "w" :type "quantitative"}
   :x {:field "x", :type "quantitative"},
   :y {:field "y", :type "quantitative"},
   :fill {:field "z", :type "nominal"}}})


(comment
  (require '[scicloj.clay.v2.api :as clay])
  (clay/make-hiccup {:source-path "notebooks/index2.clj"}))
