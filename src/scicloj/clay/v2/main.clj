(ns scicloj.clay.v2.main
  "command line interface"
  (:require [clojure.edn :as edn]
            [clojure.tools.cli :as cli]
            [scicloj.clay.v2.api :as api]))

;; TODO: option validation should be done by the API
(def cli-options
  [["-s" "--source-paths PATHS"
    :validate [sequential? (str "paths should be a sequence like " (pr-str ["notebooks"]))]
    :default-desc (pr-str ["notebooks"])
    :parse-fn edn/read-string]
   ["-t" "--target-dir DIR" :default-desc "docs"]
   ["-h" "--help"]])

(defn -main
  "Invoke with `clojure -M:dev -m scicloj.claykind.main --help` to see options"
  [& args]
  (let [{:keys [options summary arguments errors]} (cli/parse-opts args cli-options)
        {:keys [help]} options]
    (cond help (println "Clay" \newline
                        "Description: Clay evaluates Clojure namespaces into Markdown" \newline
                        "Options:" \newline
                        summary)
          errors (do (println "ERROR:" errors)
                     (System/exit -1))
          :else (do (api/make! (merge (when (seq arguments)
                                          {:paths (vec arguments)})
                                        options))
                    (System/exit 0)))))
