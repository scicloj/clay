(ns scicloj.clay.v2.api
  (:require [scicloj.clay.v2.checks :as checks]
            [scicloj.clay.v2.pipeline :as pipeline]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.clay.v2.tools :as tools]
            [scicloj.clay.v2.extensions :as extensions]
            [scicloj.clay.v2.tool.scittle]
            [scicloj.clay.v2.tool.scittle.doc :as scittle.doc]
            [scicloj.clay.v2.tool.scittle.server :as scittle.server]
            [clojure.string :as string]))


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

;; (defn show-doc-and-write!
;;   [path options]
;;   (scittle.doc/show-doc-and-write! path options))

(defn show-doc-and-write-html!
  [path options]
  (->> {:format :html}
       (merge options)
       (scittle.doc/show-doc-and-write-html! path)))

(defn show-doc-and-write-quarto!
  [path options]
  (->> {:format :quarto}
       (merge options)
       (scittle.doc/gen-doc-and-write-quarto! path)))

(defn browse!
  []
  (scittle.server/browse!))

(defn port
  []
  (scittle.server/port))

(defn url
  []
  (scittle.server/url))

(defn swap-options! [f & args]
  (apply scittle.server/swap-options!
         f args))

(defn reset-options!
  ([]
   (reset-options!  scittle.server/default-options))
  ([options]
   (scittle.server/swap-options! (constantly options))))
