(ns scicloj.clay.v2.api
  (:require [scicloj.clay.v2.checks :as checks]
            [scicloj.clay.v2.pipeline :as pipeline]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.defaults :as kindly.defaults]
            [scicloj.clay.v2.tools :as tools]
            [scicloj.clay.v2.extensions :as extensions]
            [scicloj.clay.v2.tool.scittle]
            [scicloj.clay.v2.tool.scittle.doc :as scittle.doc]
            [scicloj.clay.v2.tool.scittle.server :as scittle.server]
            [clojure.string :as string]))

(->> [:kind/hidden :kind/pprint :kind/hiccup :kind/vega :kind/vega-lite :kind/table]
     (run! kindly/add-kind!))

(kindly.defaults/setup!)

(defn check [value & predicate-and-args]
  (apply checks/check value predicate-and-args))

(def base-config
  {:tools [tools/scittle]
   :extensions []})

(defn start!
  ([]
   (start! {}))
  ([config]
   (-> base-config
       (merge config)
       pipeline/start!)
   :clay))

(defn restart!
  ([config]
   (pipeline/restart! config)
   :clay))

(defmacro capture-print
  [& body]
  `(scicloj.kindly.v3.kind/naive
    [(let [s# (new java.io.StringWriter)]
       (binding [*out* s#]
         ~@body
         (println s#)
         (str s#)))]))

(defn show-doc!
  ([path]
   (scittle.doc/show-doc! path))
  ([path options]
   (scittle.doc/show-doc! path options)))

(defn write-html!
  [path]
  (scittle.server/write-html! path))

(defn show-doc-and-write-html!
  [path options]
  (scittle.doc/show-doc-and-write-html! path options))
