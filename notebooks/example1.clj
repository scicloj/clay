^{:clay {:kindly/options {:dataset/print-range 3}}}
(ns example1
  (:require [scicloj.kindly.v4.kind :as kind]
            [tablecloth.api :as tc]
            [scicloj.kindly.v4.api :as kindly]))


(-> {:x (range 99)}
    tc/dataset
    (kind/dataset {:dataset/print-range :all}))

(-> {:x (range 99)}
    tc/dataset
    (kind/dataset {:dataset/print-range 3}))
