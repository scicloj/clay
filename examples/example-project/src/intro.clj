;; # Clay

(ns intro
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.clay.v1.tools :as tools]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind]
            [nextjournal.clerk :as clerk]))

(clay/start! {:tools [tools/clerk
                      #_tools/portal]})

(comment
  (clay/restart! {:tools [tools/clerk
                          tools/portal]}))


;; ## Kinds

;; ## Hiccup


;; ## Delays

(delay
  (Thread/sleep 500)
  (+ 1 2))

;; ## Tests

(-> 2
    (+ 3)
    (clay/check = 4))

(-> 2
    (+ 3)
    (clay/check = 5))

;; ## Images

(import java.awt.image.BufferedImage
        java.awt.Color
        sun.java2d.SunGraphics2D)

(let [n (rand-int 400)]
  (let [bi (BufferedImage. n n BufferedImage/TYPE_INT_RGB)
        g  (-> (.createGraphics ^BufferedImage bi))]
    (dotimes [t 100]
      (->> #(rand-int n)
           (repeatedly 4)
           (apply #(.drawLine ^SunGraphics2D g %1 %2 %3 %4))))
    bi))


(delay
  (-> [:div [:h2 "hi......."]]
      (kindly/consider kind/hiccup)))

;; ## Tables

(-> {:row-vectors (for [i (range 9)]
                    [i (rand)])
     :column-names [:x :y]}
    (kindly/consider kind/table))

(-> {:column-names [:x :y]
     :row-maps (for [i (range 9)]
                 {:x i
                  :y (rand)})}
    (kindly/consider kind/table))

;; ## Vega and Vega-Lite

(def vega-lite-spec
  (memoize
   (fn [n]
     (-> {:data {:values
                 (->> (repeatedly n #(- (rand) 0.5))
                      (reductions +)
                      (map-indexed (fn [x y]
                                     {:w (rand-int 9)
                                      :z (rand-int 9)
                                      :x x
                                      :y y})))},
          :mark "point"
          :encoding
          {:size {:field "w" :type "quantitative"}
           :x {:field "x", :type "quantitative"},
           :y {:field "y", :type "quantitative"},
           :fill {:field "z", :type "nominal"}}}
         (kindly/consider kind/vega)))))

(vega-lite-spec 9)

;; ## Nesting

(-> (->> [10 100 1000]
         (map (fn [n]
                [:div {:style {:width "400px"}}
                 [:h3 (str "n=" n)]
                 (vega-lite-spec n)]))
         (into [:div]))
    (kindly/consider kind/hiccup))


:bye
