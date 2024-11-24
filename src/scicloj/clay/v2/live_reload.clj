(ns scicloj.clay.v2.live-reload
  (:require [clojure.set :as set]
            [babashka.fs :as fs]
            [nextjournal.beholder :as beholder]))

(defn- subdir-paths
  "Return subdir paths of `paths1` who is a sub-directory of any `paths2`."
  [paths1 paths2]
  (let [set1 (set paths1)
        set2 (set paths2)]
    (reduce (fn [result dir1]
              (if (some #(and (not= dir1 %)
                              (fs/starts-with? dir1 %))
                        set2)
                (conj result dir1)
                result))
            #{}
            set1)))

(defn- exclude-subdirs
  "Exclude dirs who are any sub-directories of `dirs` themselves."
  [dirs]
  (let [origin-dirs (set dirs)
        to-remove (subdir-paths origin-dirs origin-dirs)]
    (set/difference origin-dirs to-remove)))

(defn- dirs-to-watch
  "Find out directories of files in `file-paths` to watch."
  [watched-dirs file-paths]
  (->> file-paths
       (map fs/parent)
       (filter #(not (some (fn [watched-dir]
                             (fs/starts-with? % watched-dir))
                           watched-dirs)))
       exclude-subdirs))

(defn- strs->paths
  "Convert strings to paths."
  [strs]
  (map fs/canonicalize strs))

(defn- paths->strs
  "Convert paths to strings."
  [paths]
  (map str paths))

(def ^:private dir-watchers-initial {:watchers {}
                                     :file-specs {}})

(def ^:private *dir-watchers (atom dir-watchers-initial))

(defn- beholder-callback!
  "Return a callback function for beholder."
  [make-fn]
  (fn [event]
    (let [canonical-path (-> event :path fs/canonicalize str)]
      (when (and (identical? :modify (:type event))
                 (contains? (:file-specs @*dir-watchers) canonical-path))
        (make-fn (get (:file-specs @*dir-watchers) canonical-path))))))

(defn- start-watching-dirs!
  "Start watching file changes in all `dirs`, with callback `cb`."
  [dirs cb]
  (doseq [dir dirs]
    (swap! *dir-watchers
           #(assoc %
                   :watchers
                   (assoc (:watchers %)
                          dir
                          (beholder/watch cb dir))))))

(defn- stop-watching-dirs!
  "Stop watching file changes in all `dirs`."
  [dirs]
  (doseq [dir dirs]
    (beholder/stop (-> @*dir-watchers :watchers (get dir)))
    (swap! *dir-watchers
           #(assoc %
                   :watchers
                   (dissoc (:watchers %) dir)))))

(defn start!
  "Watch directory changes for `source-path`."
  [make-fn {:as spec
            :keys [live-reload source-path]}]
  (when (and live-reload
             source-path)
    (let [watched-files (->> @*dir-watchers
                             :file-specs
                             keys
                             set)
          new-files (->> source-path
                         (#(if (vector? %) % [%]))
                         ;; make sure all paths are canonical,
                         ;; so that their containing directories can be properly watched by beholder
                         (map fs/canonicalize)
                         (filter #(not (contains? watched-files %)))
                         set)
          new-dirs (dirs-to-watch (strs->paths (keys (:watchers @*dir-watchers)))
                                  new-files)]
      ;; stop watching dirs now having been subdirs.
      (when-let [dirs-to-stop (subdir-paths (strs->paths (keys (:watchers @*dir-watchers)))
                                       new-dirs)]
        (stop-watching-dirs! (paths->strs dirs-to-stop)))
      ;; watch new dirs for notebook changes
      (when-not (empty? new-dirs)
        (start-watching-dirs! (paths->strs new-dirs) (beholder-callback! make-fn)))
      ;; save the spec for every file
      (when-not (empty? new-files)
        (swap! *dir-watchers #(assoc %
                                     :file-specs
                                     (->> new-files
                                          (reduce (fn [result file]
                                                    (assoc result (str file) spec))
                                                  {})
                                          (merge (:file-specs @*dir-watchers))))))
      new-files)))

(defn stop!
  "Stop all directory watchers."
  []
  (stop-watching-dirs! (-> @*dir-watchers :watchers keys))
  (reset! *dir-watchers dir-watchers-initial))

(comment
  (strs->paths ["/tmp/a/" "/tmp/b/"])
  (paths->strs (strs->paths ["/tmp/a/" "/tmp/b/"]))
  (subdir-paths (map fs/canonicalize '("/tmp/foo/a/" "/tmp/foo/a/b/" "/tmp/bar/a/" "/tmp/baz/a/"))
                (map fs/canonicalize '("/tmp/foo/" "/tmp/bar/")))
  (exclude-subdirs (map fs/canonicalize ["/tmp/a/" "/tmp/b/" "/tmp/a/foo/" "/tmp/a/bar/"]))
  (dirs-to-watch (map fs/canonicalize ["/tmp/foo/" "/tmp/bar/"])
                 (map fs/canonicalize ["/tmp/foo/a.clj" "/tmp/bar/b.clj"
                                       "/tmp/c.clj" "/tmp/d/d.clj"]))
  @*dir-watchers
  (:watchers @*dir-watchers)
  (keys (:file-specs @*dir-watchers))
  (start-watching-dirs! ["/tmp/a/" "/tmp/b/"] prn)
  (stop-watching-dirs! ["/tmp/a/" "/tmp/b/"])
  )
