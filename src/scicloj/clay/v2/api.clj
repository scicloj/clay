(ns scicloj.clay.v2.api
  (:require [scicloj.clay.v2.pipeline :as pipeline]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.clay.v2.actions :as actions]
            [scicloj.clay.v2.server :as server]
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
    (actions/show-doc! path options))
   [:ok]))

(defn show-namespace-and-write-html!
  [path options]
  (avoid-recursion
   (->> {:format :html}
        (merge options)
        (actions/show-doc-and-write-html! path))))

(defn generate-and-show-namespace-quarto!
  [path options]
  (avoid-recursion
   (->> {:format :quarto}
        (merge options)
        (actions/gen-doc-and-write-quarto! path))))

(defn generate-namespace-light-quarto!
  [path options]
  (avoid-recursion
   (->> {:format :quarto}
        (merge options)
        (actions/gen-doc-and-write-light-quarto! path))))

(defn browse!
  []
  (server/browse!))

(defn port
  []
  (server/port))

(defn url
  []
  (server/url))

(defn swap-options! [f & args]
  (apply server/swap-options!
         f args))

(defn reset-options!
  ([]
   (reset-options!  server/default-options))
  ([options]
   (server/swap-options! (constantly options))))

(defn options []
  (server/options))

(defn handle-form! [form]
  (avoid-recursion
   (pipeline/handle-form! form))
  [:ok])

(defn handle-value! [value]
  (pipeline/handle-value! value)
  [:ok])

(defn in-portal [value]
  (portal/in-portal value))
