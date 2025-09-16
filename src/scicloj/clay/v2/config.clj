(ns scicloj.clay.v2.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [scicloj.clay.v2.util.fs :as util.fs]
            [scicloj.kindly.v4.api :as kindly]))

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

(defn xdg-config-home []
  (let [env-var (System/getenv "XDG_CONFIG_HOME")]
    (if (and env-var (not (clojure.string/blank? env-var)))
      env-var
      (str (System/getProperty "user.home") "/.config"))))

(defn maybe-user-config []
  (some-> (io/file (xdg-config-home)
                   "scicloj-clay"
                   "config.edn")
          slurp-when-exists
          edn/read-string))

(defn maybe-project-config []
  (some-> "clay.edn"
          slurp-when-exists
          edn/read-string))

(defn add-field [config kw compute]
  (-> config
      (assoc kw (compute config))))

(defn implied-configs [{:keys [render source-paths keep-existing] :as config}]
  (cond-> config
          render (assoc :show false
                        :serve false
                        :browse false
                        :live-reload false)
          (and (nil? keep-existing)
               (> (count source-paths) 1)) (assoc :keep-existing true)))

(defn source-paths [{:as config :keys [base-source-path source-path render]}]
  (cond (string? source-path)
        (assoc config :source-paths [source-path])
        (sequential? source-path)
        (assoc config :source-paths source-path)
        (and render base-source-path (or (nil? source-path)
                                         (= source-path :all)))
        (assoc config :source-paths (util.fs/find-notebooks base-source-path))
        ;; nil source-path for single forms
        :else (assoc config :source-paths [nil])))

(defn merge-aliases [{:as config :keys [aliases]}]
  (reduce
    (fn [acc alias]
      (if-let [c (get acc alias)]
        (kindly/deep-merge acc c)
        (do (println "Clay warning: alias" (pr-str alias) "not found")
            acc)))
    config
    aliases))

(defn apply-conditionals [config]
  (-> config (merge-aliases) (source-paths) (implied-configs)))

(defn config
  "Gathers configuration from the default,
  a user ~/.config/scicloj-clay/config.edn if present,
  a project clay.edn if present in the current directory,
  and a spec if provided as an argument."
  ([]
   (-> (kindly/deep-merge (default-config) (maybe-user-config) (maybe-project-config))
       (apply-conditionals)))
  ([spec]
   (-> (kindly/deep-merge (default-config) (maybe-user-config) (maybe-project-config) spec)
       (apply-conditionals))))
