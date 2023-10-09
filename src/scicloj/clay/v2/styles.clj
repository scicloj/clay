(ns scicloj.clay.v2.styles
  (:require [clojure.java.io :as io]))

(def main
  (->> [:table :loader :bootstrap-toc :code]
       (map (fn [style]
              [style (->> style
                          name
                          (format "styles/%s.css")
                          io/resource
                          slurp)]))
       (into {})))


(def highlight
  (->> [:qtcreator-light] ; TODO: add all relevant themes here
       (map (fn [theme]
              [theme (->> theme
                          name
                          (format "highlight/styles/%s.min.css")
                          io/resource
                          slurp)]))
       (into {})))
