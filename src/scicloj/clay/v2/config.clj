(ns scicloj.clay.v2.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [scicloj.clay.v2.util.merge :as merge]))

(defn slurp-when-exists [path]
  (when (-> path
            io/file
            .exists)
    (-> path
        slurp)))

(defn default-config []
  (-> "clay-default.edn"
      io/resource
      slurp
      edn/read-string))

(defn maybe-user-config []
  (some-> "clay.edn"
          slurp-when-exists
          edn/read-string))

(defn add-field [config kw compute]
  (if (contains? config kw)
    config
    (assoc config kw (compute config))))

(defn config []
  (-> (default-config)
      (merge/deep-merge (maybe-user-config))))
