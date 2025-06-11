(ns scicloj.clay.v2.main
  "command line interface"
  (:gen-class)
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [scicloj.clay.v2.api :as api]
            [nrepl.cmdline]
            [scicloj.clay.v2.util.merge :as merge]))

(defn parse-aliases [s]
  (->> (str/split s #":")
       (into [] (comp (remove str/blank?)
                      (map keyword)))))

(def cli-options
  [["-h" "--help"]
   ["-r" "--render"]
   ["-t" "--base-target-path DIR" :default-desc "docs"]
   ["-c" "--config-file CONFIG-FILE" :default-desc "clay.edn"]
   ["-m" "--config-map CONFIG" :parse-fn edn/read-string :validate [map? "Must be a map"]]
   ["-A" "--reset-aliases ALIASES" :parse-fn parse-aliases]])

(defn -main
  "Invoke Clay from the command line. See https://scicloj.github.io/clay/#cli ."
  [& args]
  (let [{:keys [options summary arguments errors]} (cli/parse-opts args cli-options)
        {:keys [help config-map config-file]} options
        arg-opts (when (seq arguments)
                   (when-let [x (first (filter (complement fs/exists?) arguments))]
                     (println "Clay error:" x "does not exist")
                     (System/exit -1))
                   (let [dirs (filter fs/directory? arguments)
                         files (remove fs/directory? arguments)]
                     (cond-> {}
                             (seq files) (assoc :source-path (vec files))
                             (seq dirs) (assoc :watch-dirs (vec dirs)))))
        file-opts (when config-file
                    (when (not (fs/exists? config-file))
                      (println "Clay error:" config-file "does not exist")
                      (System/exit -1))
                    (edn/read-string (slurp config-file)))
        opts (merge/deep-merge file-opts
                               config-map
                               (select-keys options [:render :base-target-path :reset-aliases])
                               arg-opts)]
    (println "Clay options:" (pr-str opts))
    (cond help (do (println "Clay")
                   (println "Description: Clay evaluates Clojure namespaces and renders visualizations as HTML")
                   (println summary)
                   (System/exit 0))
          errors (do (println "Clay error:" errors)
                     (System/exit -1))
          :else (do (println (api/make! opts))
                    (if (:live-reload opts)
                      (nrepl.cmdline/-main)
                      (System/exit 0))))))
