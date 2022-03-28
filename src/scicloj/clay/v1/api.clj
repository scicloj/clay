(ns scicloj.clay.v1.api
  (:require [scicloj.clay.v1.checks :as checks]
            [scicloj.clay.v1.pipeline :as pipeline]
            [scicloj.clay.v1.view.clerk :as view.clerk]))

(defn check [value & predicate-and-args]
  (apply checks/check value predicate-and-args))

(defn start []
  (pipeline/start))

(defn restart []
  (pipeline/restart))

(defn setup-clerk []
  (view.clerk/setup!))
