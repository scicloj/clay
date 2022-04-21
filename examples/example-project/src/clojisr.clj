(ns clojisr
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.clay.v1.tools :as tools]
            [scicloj.clay.v1.tool.scittle :as scittle]
            [scicloj.viz.api :as viz]
            [tablecloth.api :as tc]
            [clojisr.v1.r :as r :refer [r]]
            [clojisr.v1.printing]
            [scicloj.kindly.v2.kind :as kind]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kindness :as kindness]
            [nextjournal.clerk :as clerk]))

(clay/start! {:tools [tools/clerk]})

(extend-protocol kindness/Kindness
  clojisr.v1.robject.RObject
  (kind [robject]
    :kind/naive))

(scicloj.clay.v1.tool.clerk/setup!)

(r '(rnorm 9))
