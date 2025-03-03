(ns scicloj.clay.v2.live-reload
  (:require [clojure.set :as set]
            [babashka.fs :as fs]
            [clojure.string :as str]
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
  [watched-dirs dirs file-paths]
  (-> (map (comp str fs/parent) file-paths)
      (set)
      (set/union watched-dirs dirs)
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
    (println "Clay watching:" (pr-str dirs)))
  (swap! *state update :watchers into
         (for [dir dirs]
           [dir (beholder/watch (fn watch-callback [{:as event :keys [type path]}]
                                  (let [path (str path)]
                                    (println "Clay file event:" type path)
                                    (when (and (#{:create :modify} type)
                                               (str/ends-with? (str/lower-case path) ".clj"))
                                      ;; TODO: what if the spec is a book?
                                      (make-fn (-> (or (file-spec path) spec)
                                                   (assoc :source-path path))))))
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
  (let [files (set (map (comp str fs/canonicalize)
                        (remove #(or (nil? %)
                                     (fs/directory? %))
                                source-paths)))
        dirs (set (map (comp str fs/canonicalize)
                       watch-dirs))
        watch (dirs-to-watch (watched-dirs) dirs files)]
    (doseq [dir watch]
      (when (not (fs/exists? dir))
        (fs/create-dir dir)
        (println "Clay created a directory:" dir)
        (println (str "Now you can create a Clojure file: " dir "/my_notebook.clj"))))
    (watch-files! files spec)
    ;; if we started watching a parent directory, stop watching the subdirs
    (stop-watching-dirs! (subdirs (watched-dirs) watch))
    ;; watch new dirs for notebook changes
    (watch-dirs! watch make-fn spec)))

(defn stop!
  "Stop all directory watchers."
  []
  (stop-watching-dirs! (watched-dirs))
  (reset! *state empty-state))
