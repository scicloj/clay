(ns scicloj.clay.v2.old.util.path
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [babashka.fs :as fs])
  (:import (java.nio.file LinkOption)))

(set! *warn-on-reflection* true)

(defn real-path [path]
  (let [file (io/file path)]
    (when (.exists file)
      (-> path
          io/file
          (.toPath)
          (.toRealPath (into-array LinkOption []))
          str))))

(defn path->parent [file-path]
  (-> file-path
      io/file
      (.getParent)))

(defn git-directory? [path]
  (let [dot-git-path (str path "/" ".git")]
    (->> path
         fs/list-dir
         (some (fn [path-object]
                 (-> path-object
                     str
                     (= dot-git-path)))))))

(defn git-parent [dir-path]
  (if (git-directory? dir-path)
    dir-path
    (when-not (= dir-path "/")
      (-> dir-path
          path->parent
          recur))))

(defn path-relative-to-repo [file-path]
  (when-let [gp (try (-> file-path
                         path->parent
                         real-path
                         git-parent)
                     (catch Exception e nil))]
    (-> file-path
        real-path
        (string/replace (str gp "/")
                        ""))))

(defn file-git-url [{:keys [git-url branch]}
                    file-path]
  (format "%s/blob/%s/%s"
          git-url
          branch
          file-path))
