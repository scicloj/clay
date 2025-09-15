(ns scicloj.clay.v2.files
  (:require [clojure.java.io :as io]
            [babashka.fs :as fs]))

;; will be bound when the notebook is being evaluated
(def ^:dynamic *context* nil)

(def *target-path->files (atom {}))

(defn init-target! [target-path]
  (swap! *target-path->files
         assoc target-path {}))

(defn file-path [target-path custom-name idx ext]
  (str (fs/path (str (fs/strip-ext target-path) "_files")
                (str (name custom-name) idx ext))))

(defn relative-url [target-path file]
  (-> (fs/relativize (fs/parent target-path) file)
      (fs/unixify)
      (str)))

(defn next-file!
  "Returns a pair of [file relative-path] such that one can write to file,
   and include it in a document using the relative path."
  [context custom-name value ext]
  (let [{:keys [full-target-path qmd-target-path]} context
        target-path (or qmd-target-path full-target-path)]
    (if-let [file (-> @*target-path->files
                      (get target-path)
                      (get value))]
      [file (relative-url target-path file)]
      (do (swap! *target-path->files
                 (fn [target-path->files]
                   (let [files (target-path->files target-path)]
                     (if (contains? files value)
                       target-path->files
                       (-> target-path->files
                           (assoc-in [target-path value]
                                     (file-path target-path
                                                custom-name
                                                (count files)
                                                ext)))))))
          (let [new-file (-> @*target-path->files
                             (get target-path)
                             (get value))]
            (io/make-parents new-file)
            [new-file (relative-url target-path new-file)])))))

(defn notebook-relative-file!
  "When evaluated by Clay as a notebook,
   returns a pair of [file relative-path] such that one can write to file,
   and include it in a document using the relative path.
   When evaluated outside of Clay, returns nil.
   `custom-name` will be used as a filename prefix.
   `value` is used for de-duplication, only one file is returned per value.
   `ext` is the file extension."
  [custom-name value ext]
  (when *context*
    (next-file! *context* custom-name value ext)))
