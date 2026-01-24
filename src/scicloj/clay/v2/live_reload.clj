(ns scicloj.clay.v2.live-reload
  (:require [clojure.set :as set]
            [babashka.fs :as fs]
            [clojure.string :as str]
            [nextjournal.beholder :as beholder]
            [scicloj.clay.v2.server :as server]))

(def empty-state {:watchers   {}
                  :file-specs {}})

(defonce *state (atom empty-state))

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
    (println "Clay watching:" (pr-str dirs))
    (println "  Editing .clj files in a watched directory will cause them to be rendered"))
  (swap! *state update :watchers into
         (for [dir dirs]
           [dir (beholder/watch (fn watch-callback [{:as event :keys [type path]}]
                                  (let [path (str path)
                                        ext (str/lower-case (fs/extension path))]
                                    (println "Clay file event:" type path)
                                    (when (#{:create :modify} type)
                                      (cond
                                        (= ext "clj")
                                        (make-fn (-> (or (file-spec path)
                                                         (assoc spec :source-path path))))

                                        (= ext "cljs")
                                        (do (server/scittle-eval-string! (slurp path))
                                            (println "Clay scittle:" path))))))
                                dir)])))

(defn stop-watching-dirs!
  "Stop watching file changes in all `dirs`."
  [dirs]
  (doseq [dir dirs]
    (beholder/stop (get-in @*state [:watchers dir])))
  (swap! *state update :watchers #(apply dissoc %1 %2) dirs))

;; make/make! calls start!, which sets up callbacks to make/make!
;; this is a circular dependency, so we pass make/make! in (dependency injection) instead of referring to it directly
;; source-paths and base-source-path may come from configuration rather than the spec
(defn start!
  "Watch directories of a spec"
  [make-fn {:as orig-spec :keys [base-source-path watch-dirs]} source-paths]
  (let [spec (dissoc orig-spec :live-reload)                     ;; don't need to re-watch on triggered makes
        files (set (map (comp str fs/canonicalize)
                        (remove (some-fn nil? fs/directory?)
                                source-paths)))
        ;; when nothing was specified to watch, watch the base-source-path
        watch-dirs (if (and (empty? files)
                            (empty? watch-dirs)
                            base-source-path)
                     #{base-source-path}
                     (or watch-dirs
                         ["notebooks"]))
        dirs (set (map (comp str fs/canonicalize)
                       watch-dirs))
        watch (dirs-to-watch (watched-dirs) dirs files)
        current (fs/canonicalize ".")]
    (doseq [dir watch]
      (when (not (fs/exists? dir))
        (fs/create-dir dir)
        (println "Clay created a directory:" dir)
        (println (str "Now you can create a Clojure file: " dir "/my_notebook.clj"))))
    (watch-files! files spec)
    ;; if we started watching a parent directory, stop watching the subdirs
    (stop-watching-dirs! (subdirs (watched-dirs) watch))
    ;; watch new dirs for notebook changes
    (watch-dirs! watch make-fn spec)
    (when (empty? files)
      (server/update-page! orig-spec))
    [:watching (mapv #(str (fs/relativize current %)) (watched-dirs))]))

(defn stop!
  "Stop all directory watchers."
  []
  (stop-watching-dirs! (watched-dirs))
  (reset! *state empty-state)
  [:watching nil])

(defn toggle!
  [make-fn spec source-paths]
  (if (empty? (get @*state :watchers))
    (start! make-fn spec source-paths)
    (stop!)))
