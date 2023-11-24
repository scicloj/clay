(ns scicloj.clay.v2.api
  (:require
   [clojure.string :as string]
   [clojure.test]
   [scicloj.clay.v2.actions :as actions]
   [scicloj.clay.v2.config :as config]
   [scicloj.clay.v2.quarto :as quarto]
   [scicloj.clay.v2.server :as server]
   [scicloj.clay.v2.make :as make]
   [scicloj.kindly.v4.api :as kindly]))

(defn stop! []
  (server/close!)
  [:ok])

(defn start! []
  (server/open!)
  [:ok])

(defn make! [spec]
  (start!)
  (make/make! spec))

(defn browse! []
  (server/browse!))

(defn port []
  (server/port))

(defn url []
  (server/url))

(defn config []
  (config/config))

(defn update-book! [options]
  (quarto/update-book! options))
