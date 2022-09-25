(ns scicloj.clay.v2.tool.scittle
  (:require [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kind :as kind]
            [scicloj.kindly.v3.kindness :as kindness]
            [scicloj.clay.v2.walk]
            [scicloj.clay.v2.tool :as tool]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [clojure.string :as string]
            [clojure.java.browse :as browse]
            [scicloj.clay.v2.html.table :as table]
            [clojure.java.io :as io]
            [scicloj.clay.v2.tool.scittle.server :as server]
            [scicloj.clay.v2.tool.scittle.doc :as doc]))

(def tool
  (reify tool/Tool
    (setup! [this config])
    (open! [this]
      (server/open!))
    (close! [this]
      (server/close!))
    (show! [this context]
      (server/show! context))))

(defn show-widgets!
  ([widgets]
   (server/show-widgets! widgets))
  ([widgets options]
   (server/show-widgets! widgets options)))

(defn show-doc!
  ([path]
   (doc/show-doc! path))
  ([path options]
   (doc/show-doc! path options)))

(defn write-html!
  [path]
  (server/write-html! path))

;; It is convenient to refresh all clients on re-evaluation.
(server/broadcast! "refresh")
