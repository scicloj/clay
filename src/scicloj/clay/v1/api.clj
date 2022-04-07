(ns scicloj.clay.v1.api
  (:require [scicloj.clay.v1.checks :as checks]
            [scicloj.clay.v1.pipeline :as pipeline]
            [scicloj.clay.v1.tool.clerk :as view.clerk]
            [scicloj.kindly.v2.api :as kindly]))

(->> [:kind/naive :kind/hiccup :kind/vega :kind/vega-lite :kind/table]
     (run! kindly/define-kind!))

(defn check [value & predicate-and-args]
  (apply checks/check value predicate-and-args))

(defn start! [config]
  (pipeline/start! config))

(defn restart! [config ]
  (pipeline/restart! config))
