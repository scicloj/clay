(ns scicloj.clay.v2.live-reload
  (:require [clojure.set :as set]
            [babashka.fs :as fs]
            [nextjournal.beholder :as beholder]))

(def empty-state {:watchers   {}
                  :file-specs {}})

(def *state (atom empty-state))

(defn subdir? [dir root]
  (and (not= dir root)
       (fs/starts-with? dir root)))

(defn subdirs
  "Return a subset of `paths1` that are subdirectories of any `paths2`."
  [paths1 paths2]
  (reduce (fn [acc path]
            (if (some #(subdir? path %) paths2)
              (conj acc path)
              acc))
          #{}
          paths1))

(defn roots
  "Remove from paths any subdirectories."
  [paths]
  (set/difference paths (subdirs paths paths)))

(defn dirs-to-watch
  "Returns any parents of file-paths that are not in watched-dirs."
  [watched-dirs file-paths]
  (-> (map (comp str fs/parent) file-paths)
      (set)
      (set/union watched-dirs)
      (roots)
      (set/difference watched-dirs)))

(defn watched-dirs []
  (-> @*state :watchers keys set))

(defn watched-files []
  (-> @*state :file-specs keys set))

(defn file-spec [file]
  (get-in @*state [:file-specs file]))

(defn watch-files! [files spec]
  (swap! *state update :file-specs into
         (for [file files]
           [file spec])))

(defn watch-dirs!
  "Start watching file changes in `dirs` with make."
  [dirs make-fn spec]
  {:pre [(empty? (set/intersection (set dirs) (watched-dirs)))]}
  (when (seq dirs)
    (println "WATCHING:" (pr-str dirs)))
  (swap! *state update :watchers into
         (for [dir dirs]
           [dir (beholder/watch (fn watch-callback [{:as event :keys [type path]}]
                                  (println "EVENT:" type (str path))
                                  ;; TODO; handle deleted??
                                  (when (#{:create :modify} type)
                                    ;; TODO: what if the spec is a book?
                                    (make-fn (-> (or (file-spec (str path)) spec)
                                                 (assoc :source-path (str path))))))
                                dir)])))

(defn stop-watching-dirs!
  "Stop watching file changes in all `dirs`."
  [dirs]
  (doseq [dir dirs]
    (beholder/stop (get-in @*state [:watchers dir])))
  (swap! *state update :watchers dissoc dirs))

;; make/make! calls start!, which sets up callbacks to make/make!
;; this is a circular dependency, so we pass make/make! in (dependency injection) instead of referring to it directly
;; source-paths and base-source-path may come from configuration rather than the spec
(defn start!
  "Watch directories of a spec"
  [make-fn spec source-paths watch-dirs]
  (let [canonical-paths (set (map (comp str fs/canonicalize)
                                  (concat
                                    (map #(babashka.fs/path % "unnamed.clj") watch-dirs)
                                    (remove nil? source-paths))))
        dirs (dirs-to-watch (watched-dirs) canonical-paths)]
    ;; TODO: maybe make a directory instead?
    (when-let [bad-path (first (filter (complement babashka.fs/exists?) canonical-paths))]
      (throw (ex-info (str "Does not exist: " bad-path)
                      {:id   ::bad-path
                       :path bad-path})))
    (watch-files! canonical-paths spec)
    ;; if we started watching a parent directory, stop watching the subdirs
    (stop-watching-dirs! (subdirs (watched-dirs) dirs))
    ;; watch new dirs for notebook changes
    (watch-dirs! dirs make-fn spec)))

(defn stop!
  "Stop all directory watchers."
  []
  (stop-watching-dirs! (watched-dirs))
  (reset! *state empty-state))
