(ns scicloj.clay.v2.files
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [babashka.fs :as fs]))

(def *target-path->files (atom {}))

(defn init-target! [target-path]
  (swap! *target-path->files
         assoc target-path {}))

(defn file-path [target-path custom-name idx ext]
  (str (-> target-path
           (string/replace #"\.html$" ""))
       "_files/"
       (name custom-name)
       idx
       ext))

(defn next-file! [target-path custom-name value ext]
  (if-let [file (-> @*target-path->files
                    (get target-path)
                    (get value))]
    file
    ;; else
    (do (swap! *target-path->files
               (fn [target-path->templfiles]
                 (let [files (target-path->templfiles target-path)]
                   (if-let [file (get files value)]
                     target-path->templfiles
                     (-> target-path->templfiles
                         (assoc-in [target-path value]
                                   (file-path target-path
                                              custom-name
                                              (count files)
                                              ext)))))))
        (let [new-file (-> @*target-path->files
                           (get target-path)
                           (get value))]
          (io/make-parents new-file)
          new-file))))

(defn relative-url [full-target-path file]
  (-> (fs/relativize (fs/parent full-target-path)
                     file)
      (fs/unixify)
      (str)))
