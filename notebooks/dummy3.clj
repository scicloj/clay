(ns dummy3
  (:require [fastmath.random :as random]
            [fastmath.stats :as stats]
            [fastmath.core :as math]))

(let [true-mean 20
      data [21 23]
      sample-mean (/ (+ 21 23) 2)
      ;; we can compute from the data:
      variance1 (/ (+ (math/sq (- 21 sample-mean))
                      (math/sq (- 23 sample-mean)))
                   2)
      ;; we cannot compute from the data:
      variance2 (/ (+ (math/sq (- 21 true-mean))
                      (math/sq (- 23 true-mean)))
                   2)
      ;; we can compute from the data:
      variance3 (/ (+ (math/sq (- 21 sample-mean))
                      (math/sq (- 23 sample-mean)))
                   (- 2 1))]
  {:sample-mean sample-mean
   :variance1 variance1
   :variance2 variance2
   :variance3 variance3})



(defn random-experime (let [true-mean 20
                            x1 (+ true-mean (random/grand))
                            x2 (+ true-mean (random/grand))
                            sample-mean (/ (+ x1 x2) 2)
                            ;; we can compute from the data:
                            variance1 (/ (+ (math/sq (- x1 sample-mean))
                                            (math/sq (- x2 sample-mean)))
                                         2)
                            ;; we cannot compute from the data:
                            variance2 (/ (+ (math/sq (- x1 true-mean))
                                            (math/sq (- x2 true-mean)))
                                         2)
                            ;; we can compute from the data:
                            variance3 (/ (+ (math/sq (- x1 sample-mean))
                                            (math/sq (- x2 sample-mean)))
                                         (- 2 1))]
                        {:true-mean true-mean
                         :sample-mean sample-mean
                         :variance1 variance1
                         :variance2 variance2
                         :variance3 variance3}))

(random-experiment)

[(->> (repeatedly 100000 random-experiment)
      (map :variance1)
      stats/mean)

 (->> (repeatedly 100000 random-experiment)
      (map :variance2)
      stats/mean)

 (->> (repeatedly 100000 random-experiment)
      (map :variance3)
      stats/mean)]
