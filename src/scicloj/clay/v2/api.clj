(ns scicloj.clay.v2.api
  (:require [scicloj.clay.v2.pipeline :as pipeline]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.clay.v2.actions :as actions]
            [scicloj.clay.v2.quarto :as quarto]
            [scicloj.clay.v2.server :as server]
            [scicloj.clay.v2.state :as state]
            [scicloj.clay.v2.config :as config]
            [clojure.string :as string]
            [clojure.test]))

(defn stop! []
  (pipeline/stop!)
  [:ok])

(defn start! []
  (pipeline/start!)
  [:ok])

(defn show-namespace!
  ([path]
   (show-namespace! path nil))
  ([path options]
   (start!)
   (actions/show-doc! path options)
   [:ok]))

(defn show-namespace-and-write-html!
  [path options]
  (->> {:format :html}
       (merge options)
       (actions/show-doc-and-write-html! path)))

(defn render-namespace-quarto!
  [path options]
  (->> {:format :quarto}
       (merge options)
       (actions/render-quarto! path)))

(defn write-namespace-quarto!
  [path options]
  (->> {:format :quarto}
       (merge options)
       (actions/write-quarto! path)))

(defn browse! []
  (server/browse!))

(defn port []
  (state/port))

(defn url []
  (server/url))

(defn config []
  (config/config))

(defn handle-form! [form]
  (pipeline/handle-form! form)
  [:ok])

(defn handle-value! [value]
  (pipeline/handle-value! value)
  [:ok])

(defn update-book! [options]
  (quarto/update-book! options))
