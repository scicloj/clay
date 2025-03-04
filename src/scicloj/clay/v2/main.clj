(ns scicloj.clay.v2.main
  "command line interface"
  (:gen-class)
  (:require [babashka.fs :as fs]
            [clojure.tools.cli :as cli]
            [scicloj.clay.v2.api :as api]
            [nrepl.cmdline]))

(def cli-options
  [["-t" "--base-target-path DIR" :default-desc "docs"]
   ["-r" "--render"]
   ["-h" "--help"]])

(def default-options
  {:live-reload true})

(def render-options
  {:show        false
   :serve       false
   :live-reload false})

(defn -main
  "Invoke Clay from the command line. See https://scicloj.github.io/clay/#cli ."
  [& args]
  (let [{:keys [options summary arguments errors]} (cli/parse-opts args cli-options)
        {:keys [help render]} options
        opts (merge {:watch-dirs ["notebooks"]}
                    (when (seq arguments)
                      (when-let [x (first (filter (complement fs/exists?) arguments))]
                        (println "Clay error:" x "does not exist")
                        (System/exit -1))
                      (let [dirs (filter fs/directory? arguments)
                            files (remove fs/directory? arguments)]
                        (cond-> {}
                          (seq files) (assoc :source-path (vec files))
                          (seq dirs) (assoc :watch-dirs (vec dirs)))))
                    (dissoc options :render)
                    (if render
                      render-options
                      default-options))]
    (println "Clay options:" (pr-str opts))
    (cond help (do (println "Clay")
                   (println "Description: Clay evaluates Clojure namespaces and renders visualizations as HTML")
                   (println summary)
                   (System/exit 0))
          errors (do (println "Clay error:" errors)
                     (System/exit -1))
          :else (do (println
                     (api/make! opts))
                    (if (:live-reload opts)
                      (nrepl.cmdline/-main)
                      (System/exit 0))))))
