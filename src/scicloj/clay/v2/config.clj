(ns scicloj.clay.v2.config
  (:require [clojure.java.io :as io]))

(defn slurp-when-exists [path]
  (when (-> path
            io/file
            .exists)
    (-> path
        slurp)))

(defn config
  ([] (read-string
       (or (-> "clay.edn"
               slurp-when-exists)
           (-> "clay-default.edn"
               io/resource
               slurp))))
  ([k] ((config) k)))
