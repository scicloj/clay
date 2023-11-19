(ns scicloj.clay.v2.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn slurp-when-exists [path]
  (when (-> path
            io/file
            .exists)
    (-> path
        slurp)))

(defn config
  ([] (merge (-> "clay-default.edn"
                 io/resource
                 slurp
                 edn/read-string)
             (some-> "clay.edn"
                     slurp-when-exists
                     edn/read-string))))
