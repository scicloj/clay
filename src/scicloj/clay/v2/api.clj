(ns scicloj.clay.v2.api
  (:require [scicloj.clay.v2.checks :as checks]
            [scicloj.clay.v2.pipeline :as pipeline]
            [scicloj.clay.v2.view.image]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.defaults :as kindly.defaults]
            [scicloj.clay.v2.tools :as tools]
            [scicloj.clay.v2.extensions :as extensions]))

(->> [:kind/hidden :kind/pprint :kind/hiccup :kind/vega :kind/vega-lite :kind/table]
     (run! kindly/add-kind!))

(kindly/set-only-advice! #'kindly.defaults/advice)

(defn check [value & predicate-and-args]
  (apply checks/check value predicate-and-args))

(defn start!
  ([]
   (start! {}))
  ([config]
   (-> {:tools [tools/scittle]
        :extensions [extensions/dataset]}
       (merge config)
       pipeline/start!)
   [:ok]))

(defn restart! [config ]
  (pipeline/restart! config)
  [:ok])

(defmacro capture-print
  [& body]
  `(scicloj.kindly.v3.kind/naive
    [(let [s# (new java.io.StringWriter)]
       (binding [*out* s#]
         ~@body
         (println s#)
         (str s#)))]))
