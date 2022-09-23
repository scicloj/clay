(ns scicloj.clay.v2.path
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (java.nio.file LinkOption)))


(defn real-path [path]
  (let [file (io/file path)]
    (when (.exists file)
      (-> path
          io/file
          (.toPath)
          (.toRealPath (into-array LinkOption []))
          str))))
