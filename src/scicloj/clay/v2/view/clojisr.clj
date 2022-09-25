(ns scicloj.clay.v2.view.clojisr
  (:require [clojisr.v1.robject]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kindness :as kindness]))

(extend-protocol kindness/Kindness
  clojisr.v1.robject.RObject
  (kind [robject]
    :kind/pprint))
