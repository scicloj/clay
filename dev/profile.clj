(ns profile
  (:require [clj-async-profiler.core :as prof]))

(defn -main []
  (println "Profiling...")
  (prof/profile (require '[scicloj.clay.v2.api]))
  (println "Done. See /tmp/clj-async-profiler/results/"))
