(ns scicloj.clay.v2.files
  (:require [clojure.string :as string]))

(def *target-path->files (atom {}))

(defn init-target! [target-path]
  (swap! *target-path->files
         assoc target-path {}))

(defn file-path [target-path idx ext]
  (str (-> target-path
           #_(string/replace #"\.html$" ""))
       "_files/"
       idx
       ext))

(defn next-file! [target-path value ext]
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
                                              (count files)
                                              ext)))))))
        (-> @*target-path->files
            (get target-path)
            (get value)))))
