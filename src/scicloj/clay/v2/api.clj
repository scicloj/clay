(ns scicloj.clay.v2.api
  (:require [scicloj.clay.v2.checks :as checks]
            [scicloj.clay.v2.pipeline :as pipeline]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.clay.v2.tools :as tools]
            [scicloj.clay.v2.extensions :as extensions]
            [scicloj.clay.v2.tool.scittle]
            [scicloj.clay.v2.tool.scittle.doc :as scittle.doc]
            [scicloj.clay.v2.tool.scittle.server :as scittle.server]
            [clojure.string :as string]
            [clojure.test]))

(def ^:dynamic *in-api-call?* false)

(def invisible-ok
  (kindly/consider
   [:ok]
   :kind/void))

(defmacro avoid-recursion [& forms]
  `(do (if-not *in-api-call?*
         (binding [*in-api-call?* true]
           ~@forms))))

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
   invisible-ok))

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

(defn show-namespace!
  ([path]
   (show-namespace! path nil))
  ([path options]
   (avoid-recursion
    (start!)
    (scittle.doc/show-doc! path options))
   invisible-ok))

(defn write-html!
  [path]
  (scittle.server/write-html! path))

(defn show-namespace-and-write-html!
  [path options]
  (avoid-recursion
   (->> {:format :html}
        (merge options)
        (scittle.doc/show-doc-and-write-html! path))))

(defn generate-and-show-namespace-quarto!
  [path options]
  (avoid-recursion
   (->> {:format :quarto}
        (merge options)
        (scittle.doc/gen-doc-and-write-quarto! path))))

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

(defn options []
  (scittle.server/options))

(defn handle-form! [form]
  (avoid-recursion
   (pipeline/handle-form! form))
  invisible-ok)

(defn handle-value! [value]
  (pipeline/handle-value! value)
  invisible-ok)
