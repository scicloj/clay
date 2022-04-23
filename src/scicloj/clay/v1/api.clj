(ns scicloj.clay.v1.api
  (:require [scicloj.clay.v1.checks :as checks]
            [scicloj.clay.v1.pipeline :as pipeline]
            [scicloj.clay.v1.tool.clerk :as view.clerk]
            [scicloj.clay.v1.view.image]
            [scicloj.kindly.v2.api :as kindly]))

(->> [:kind/naive :kind/hiccup :kind/vega :kind/vega-lite :kind/table]
     (run! kindly/define-kind!))

(defn check [value & predicate-and-args]
  (apply checks/check value predicate-and-args))

(defn start! [config]
  (pipeline/start! config))

(defn restart! [config ]
  (pipeline/restart! config))

(defmacro capture-print
  [& body]
  `(scicloj.kindly.v2.kind/naive
    [(let [s# (new java.io.StringWriter)]
       (binding [*out* s#]
         ~@body
         (println s#)
         (str s#)))]))
