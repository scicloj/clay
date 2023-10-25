(ns scicloj.clay.v2.config
  (:require [scicloj.clay.v2.util.merge :as merge]
            [clojure.java.io :as io]))

(defn slurp-when-exists [path]
  (when (-> path
            io/file
            .exists)
    (-> path
        slurp)))

(defn config
  ([] (merge/deep-merge (some-> "clay.edn"
                                slurp-when-exists
                                read-string)
                        (-> "clay-default.edn"
                            io/resource
                            slurp
                            read-string)))
  ([k] ((config) k)))
