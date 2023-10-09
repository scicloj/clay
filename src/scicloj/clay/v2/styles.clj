(ns scicloj.clay.v2.styles
  (:require [clojure.java.io :as io]))

(def table
  (-> "styles/table.css"
      io/resource
      slurp))

(def loader
  (-> "styles/loader.css"
      io/resource
      slurp))

(def bootstrap-toc
  (-> "styles/bootstrap-toc.css"
      io/resource
      slurp))

(def highlight
  (->> [:qtcreator-light] ; TODO: add all relevant themes here
       (map (fn [theme]
              [theme (->> theme
                          name
                          (format "highlight/styles/%s.min.css")
                          io/resource
                          slurp)]))
       (into {})))
