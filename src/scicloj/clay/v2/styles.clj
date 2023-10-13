(ns scicloj.clay.v2.styles
  (:require [clojure.java.io :as io]))

(defn main [style]
  (->> style
       name
       (format "styles/%s.css")
       io/resource
       slurp))


(defn highlight [theme]
  (->> theme
       name
       (format "highlight/styles/%s.min.css")
       io/resource
       slurp))
