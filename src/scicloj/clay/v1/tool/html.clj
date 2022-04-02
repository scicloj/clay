(ns scicloj.clay.v1.tool.html
  (:require
   [scicloj.kindly.v2.api :as kindly]
   [scicloj.kindly.v2.kind :as kind]
   [scicloj.clay.v1.hiccups :as hiccups]
   [scicloj.clay.v1.tool.clerk :as tool.clerk]
   [scicloj.clay.v1.tool :as tool]
   [scicloj.clay.v1.tool.html.table :as table]
   [nextjournal.clerk :as clerk]
   [hiccup.core :as hiccup]
   [hiccup.page :as page]))

(defn maybe-apply-viewer [value kind]
  (if-let [viewer (-> kind kindly/kind->behaviour :html.viewer)]
    (viewer value)
    value))

(defn prepare [value]
  (->> value
       kindly/kind
       (maybe-apply-viewer value)))

(defn show! [value code]
  (-> value
      prepare
      (->> (into [:body]))
      page/html5
      hiccups/in-iframe
      clerk/html
      (tool.clerk/show! code)))

(def tool
  (reify tool/Tool
    (setup! [this config])
    (open! [this]
      (clerk/serve! {:browse? true}))
    (close! [this])
    (show! [this value code]
      (show! value code))))

(kindly/define-kind-behaviour! :kind/hiccup
  {:html.viewer #(hiccup/html %)})

(kindly/define-kind-behaviour! :kind/table
  {:html.viewer (fn [table-spec]
                  (-> table-spec
                      table/->table-hiccup
                      table/table-hiccup->datatables-html))})
