(ns scicloj.clay.v2.live-reload
  (:require [clojure.java.io :as io]
            [scicloj.clay.v2.make :as make]
            [nextjournal.beholder :as beholder]))

(defonce dir-watchers-initial {:watchers []
                               :watched-dirs #{}
                               :file-specs {}})

(defonce *dir-watchers (atom dir-watchers-initial))

(defn stop-watchers
  "Stop all directory watchers."
  []
  (doseq [w (:watchers @*dir-watchers)]
    (beholder/stop w))
  (reset! *dir-watchers dir-watchers-initial))

(defn- beholder-callback
  "Callback function for beholder."
  [event]
  (let [canonical-path (str (-> event :path .toFile .getCanonicalPath))]
    (when (and (identical? :modify (:type event))
               (contains? (:file-specs @*dir-watchers) canonical-path))
      (make/make! (get (:file-specs @*dir-watchers) canonical-path)))))

(defn watch-dir
  "Watch directory changes if necessary."
  [{:as spec
    :keys [live-reload source-path]}]
  (when (and live-reload
             source-path)
    (let [->canonical-path (fn [file] (.getCanonicalPath (io/file file)))
          watched-files (->> @*dir-watchers
                             :file-specs
                             keys
                             set)
          new-files (->> source-path
                         (#(if (vector? %) % [%]))
                         ;; make sure all paths are canonical,
                         ;; so that their containing directories can be properly watched by beholder
                         (map ->canonical-path)
                         (filter #(not (contains? watched-files %)))
                         set)
          new-dirs (->> new-files
                        (map #(-> % io/file .getParent))
                        (filter #(not (some (fn [watched-dir]
                                              (.startsWith % watched-dir))
                                            (:watched-dirs @*dir-watchers))))
                        set)]
      ;; watch dir for notebook changes
      (when-not (empty? new-dirs)
        (swap! *dir-watchers
               #(assoc %
                       :watched-dirs
                       (into (:watched-dirs %) new-dirs)
                       :watchers
                       (conj (:watchers %)
                             (apply beholder/watch
                                    beholder-callback
                                    new-dirs)))))
      ;; save the spec for every file
      (when-not (empty? new-files)
        (swap! *dir-watchers #(assoc %
                                     :file-specs
                                     (->> new-files
                                          (reduce (fn [pre-result file]
                                                    (assoc pre-result
                                                           file
                                                           spec))
                                                  {})
                                          (merge (:file-specs @*dir-watchers))))))
      new-files)))
