(ns scicloj.clay.v2.tempfiles
  (:require [clojure.string :as string]))

(def *target-path->tempfiles (atom {}))

(defn init-target! [target-path]
  (swap! *target-path->tempfiles
         assoc target-path {}))

(defn tempfile-path [target-path idx ext]
  (str (or (some-> target-path
                   (string/replace #"\.html$" ""))
           "docs/")
       "_files/"
       idx
       ext))

(defn next-tempfile! [target-path value ext]
  (if-let [tempfile (-> @*target-path->tempfiles
                        (get target-path)
                        (get value))]
    tempfile
    ;; else
    (do (swap! *target-path->tempfiles
               (fn [target-path->templfiles]
                 (let [tempfiles (target-path->templfiles target-path)]
                   (if-let [tempfile (get tempfiles value)]
                     target-path->templfiles
                     (-> target-path->templfiles
                         (assoc-in [target-path value]
                                   (tempfile-path target-path
                                                  (count tempfiles)
                                                  ext)))))))
        (-> @*target-path->tempfiles
            (get target-path)
            (get value)))))
