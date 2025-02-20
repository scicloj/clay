(ns scicloj.clay.v2.main
  "command line interface"
  (:require [clojure.edn :as edn]
            [clojure.tools.cli :as cli]
            [scicloj.clay.v2.api :as api]))

(def cli-options
  [["-s" "--source-path PATHS"
    :default-desc (pr-str ["notebooks"])
    :parse-fn edn/read-string]
   ["-t" "--base-target-path DIR"
    :default-desc "docs"]
   ["-h" "--help"]])

(defn -main
  "Invoke with `clojure -M:dev -m scicloj.clay.v2.main --help` to see options"
  [& args]
  (let [{:keys [options summary arguments errors]} (cli/parse-opts args cli-options)
        {:keys [help]} options
        opts (merge (when (seq arguments)
                      {:source-path (vec arguments)})
                    options)]
    (println "Options:")
    (pr-str opts)
    (cond help (do (println "Clay")
                   (println "Description: Clay evaluates Clojure namespaces and renders visualizations as HTML")
                   (println summary)
                   (System/exit 0))
          errors (do (println "Error:" errors)
                     (System/exit -1))
          :else (do (api/make! opts)
                    (System/exit 0)))))
