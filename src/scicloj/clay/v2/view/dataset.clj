(ns scicloj.clay.v2.view.dataset
  (:require [tech.v3.dataset :as tmd]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kindness :as kindness]))

(extend-protocol kindness/Kindness
  tech.v3.dataset.impl.dataset.Dataset
  (kind [this]
    :kind/dataset))
