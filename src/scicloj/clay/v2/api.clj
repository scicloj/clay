(ns scicloj.clay.v2.api
  (:require [scicloj.clay.v2.pipeline :as pipeline]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.clay.v2.doc :as scittle.doc]
            [scicloj.clay.v2.server :as scittle.server]
            [scicloj.clay.v2.portal :as portal]
            [clojure.string :as string]
            [clojure.test]))

(def ^:dynamic *in-api-call?* false)

(defmacro avoid-recursion [& forms]
  `(do (if-not *in-api-call?*
         (binding [*in-api-call?* true]
           ~@forms))))

(defn stop! []
  (pipeline/stop!)
  [:ok])

(defn start! []
  (pipeline/start!)
  [:ok])

(defmacro capture-print
  [& body]
  `(scicloj.kindly.v4.kind/pprint
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
   [:ok]))

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

(defn generate-namespace-light-quarto!
  [path options]
  (avoid-recursion
   (->> {:format :quarto}
        (merge options)
        (scittle.doc/gen-doc-and-write-light-quarto! path))))

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
  [:ok])

(defn handle-value! [value]
  (pipeline/handle-value! value)
  [:ok])

(defn in-portal [value]
  (portal/in-portal value))
