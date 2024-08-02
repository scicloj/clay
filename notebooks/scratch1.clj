;; # Testing

(ns scratch1
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]))

;; ## A section

;; Some text.
;;
;; Some more text.

(kind/portal {:x (range 3)})

(kind/vega-lite
 {:data {:url "notebooks/datasets/iris.csv"},
  :mark "rule",
  :encoding {:opacity {:value 0.2}
             :size {:value 3}
             :x {:field "sepal_width", :type "quantitative"},
             :x2 {:field "sepal_length", :type "quantitative"},
             :y {:field "petal_width", :type "quantitative"},
             :y2 {:field "petal_length", :type "quantitative"},
             :color {:field "species", :type "nominal"}}
  :background "floralwhite"})

(kind/pprint
 {:data {:url "notebooks/datasets/iris.csv"},
  :mark "rule",
  :encoding {:opacity {:value 0.2}
             :size {:value 3}
             :x {:field "sepal_width", :type "quantitative"},
             :x2 {:field "sepal_length", :type "quantitative"},
             :y {:field "petal_width", :type "quantitative"},
             :y2 {:field "petal_length", :type "quantitative"},
             :color {:field "species", :type "nominal"}}
  :background "floralwhite"})
