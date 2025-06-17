(ns scicloj.clay.v2.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [scicloj.clay.v2.util.fs :as util.fs]
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
          edn/read-string
          ;; allow code execution in configuration
          eval))

(defn add-field [config kw compute]
  (-> config
      (assoc kw (compute config))))

(defn implied-configs [config]
  (cond-> config
          (:render config) (merge {:show        false
                                   :serve       false
                                   :browse      false
                                   :live-reload false})))

(defn source-paths [{:as config :keys [base-source-path source-path render]}]
  (cond-> config
          (and base-source-path (or (nil? source-path) (#{:all} source-path)) render)
          (assoc :source-paths (util.fs/find-notebooks base-source-path))

          (string? source-path)
          (assoc :source-paths [source-path])))

(defn merge-aliases [{:as config :keys [aliases]}]
  (reduce
    (fn [acc alias]
      (if-let [c (get acc alias)]
        (merge/deep-merge acc c)
        (do (println "Clay warning: alias" (pr-str alias) "not found")
            acc)))
    config
    aliases))

(defn apply-conditionals [config]
  (-> config (merge-aliases) (implied-configs) (source-paths)))

(defn config
  "Gathers configuration from the default, a clay.edn, and a spec if provided"
  ([]
   (-> (merge/deep-merge (default-config) (maybe-user-config))
       (apply-conditionals)))
  ([spec]
   (-> (merge/deep-merge (default-config) (maybe-user-config) spec)
       (apply-conditionals))))
