^{:clay {:kindly/options {:dataset/print-range 3}}}
(ns index1
  (:require [scicloj.kindly.v4.kind :as kind]
            [tablecloth.api :as tc]))


(tc/dataset {:x (range 99)})
