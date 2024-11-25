(ns scicloj.clay.v2.live-reload
  (:require [clojure.set :as set]
            [babashka.fs :as fs]
            [nextjournal.beholder :as beholder]))

(defn subdir-paths
  "Return subdir paths of `paths1` who is a sub-directory of any `paths2`."
  [paths1 paths2]
  (reduce (fn [result dir1]
            (if (some #(and (not= dir1 %)
                            (fs/starts-with? dir1 %))
                      paths2)
              (conj result dir1)
              result))
          #{}
          paths1))

(defn exclude-subdir-paths
  "Exclude paths who are sub-directories of any one of `paths`."
  [paths]
  (let [origin-dirs (set paths)
        to-remove (subdir-paths origin-dirs origin-dirs)]
    (set/difference origin-dirs to-remove)))

(defn dirs-to-watch
  "Find out directories of files in `file-paths` to watch."
  [watched-dir-paths file-paths]
  (->> file-paths
       (map fs/parent)
       (filter #(not (some (fn [watched-dir]
                             (fs/starts-with? % watched-dir))
                           watched-dir-paths)))
       exclude-subdir-paths))

(comment
  (defn strs->paths
    "Convert strings to paths."
    [strs]
    (map fs/canonicalize strs))

  (defn paths->strs
    "Convert paths to strings."
    [paths]
    (map str paths)))

(def ^:private dir-watchers-initial {:watchers {}
                                     :file-specs {}})

(def ^:private *dir-watchers (atom dir-watchers-initial))

(defn beholder-callback!
  "Return a callback function for beholder."
  [make-fn]
  (fn [event]
    (let [canonical-path (-> event :path fs/canonicalize)
          spec (get (:file-specs @*dir-watchers) canonical-path)]
      (when (and spec (identical? :modify (:type event)))
        (make-fn spec)))))

(defn watched-dirs!
  "Get all watched dirs as paths."
  []
  (->> @*dir-watchers :watchers keys set))

(defn watched-files!
  "Get all watched dirs as paths."
  []
  (->> @*dir-watchers :file-specs keys set))

(defn start-watching-dirs!
  "Start watching file changes in all `dirs`, with callback `cb`."
  [dirs cb]
  (doseq [dir dirs]
    (when-not (contains? (watched-dirs!) dir)
      (swap! *dir-watchers
             #(assoc %
                     :watchers
                     (assoc (:watchers %)
                            dir
                            (beholder/watch cb (str dir))))))))

(defn stop-watching-dirs!
  "Stop watching file changes in all `dirs`."
  [dirs]
  (doseq [dir dirs]
    (when-let [watcher (-> @*dir-watchers :watchers (get dir))]
      (beholder/stop watcher)
      (swap! *dir-watchers
             #(assoc %
                     :watchers
                     (dissoc (:watchers %) dir))))))

(defn start!
  "Watch directory changes for `source-path`."
  [make-fn {:as spec
            :keys [live-reload source-path]}]
  (when (and live-reload
             source-path)
    (let [new-files (->> source-path
                         (#(if (vector? %) % [%]))
                         ;; make sure all paths are canonical,
                         ;; so that their containing directories can be properly watched by beholder
                         (map fs/canonicalize)
                         (filter #(not (contains? (watched-files!) %)))
                         set)
          new-dirs (dirs-to-watch (watched-dirs!)
                                  new-files)]
      ;; stop watching dirs now having been subdirs.
      (when-let [dirs-to-stop (subdir-paths (watched-dirs!)
                                            new-dirs)]
        (stop-watching-dirs! dirs-to-stop))
      ;; watch new dirs for notebook changes
      (when-not (empty? new-dirs)
        (start-watching-dirs! new-dirs (beholder-callback! make-fn)))
      ;; save the spec for every file
      (when-not (empty? new-files)
        (swap! *dir-watchers #(assoc %
                                     :file-specs
                                     (->> new-files
                                          (reduce (fn [result file]
                                                    (assoc result file spec))
                                                  {})
                                          (merge (:file-specs @*dir-watchers))))))
      new-files)))

(defn stop!
  "Stop all directory watchers."
  []
  (stop-watching-dirs! (watched-dirs!))
  (reset! *dir-watchers dir-watchers-initial))
