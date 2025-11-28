(ns scicloj.clay.v2.util.diff
  (:require [clojure.string :as str]
            [clojure.pprint :as pp]
            [babashka.fs :as fs]
            [lambdaisland.deep-diff2 :as ddiff]))

(defn- print-diffs [diff-print-fn note-diffs]
  (doseq [diff note-diffs]
    (diff-print-fn diff)))

(defn- write-diff-files [old new note-diffs diff-print-fn print-fn
                        {:diff/keys [keep-dirs
                                     timestamp]
                         :keys [full-source-path]
                         :as spec}]
  (let [diffs-base-path (fs/absolutize "read-kinds-diffs")
        diffs-path (-> diffs-base-path
                       (fs/path timestamp))
        diff-base-file (-> full-source-path
                           (str/replace "/" ".")
                           (->> (fs/path diffs-path)
                                str))
        diff-file (str diff-base-file ".edn")
        old-file (str diff-base-file ".old.edn")
        new-file (str diff-base-file ".new.edn")
        diff-dirs (->> (fs/list-dir diffs-base-path
                                    #(fs/directory? % {:nofollow-links true}))
                       (sort-by fs/last-modified-time))]
    (when (number? keep-dirs)
      (doseq [dir (take (max 0 (inc (- (count diff-dirs) keep-dirs)))
                        diff-dirs)]
        (when (= (fs/parent dir) diffs-base-path)
          (fs/delete-tree dir))))
    (fs/create-dirs diffs-path)
    (println "creating diff file " diff-file " & old/new")
    (when diff-print-fn
      (spit diff-file
            (with-out-str
              (print-diffs diff-print-fn note-diffs))))
    (spit old-file (with-out-str (print-fn old)))
    (spit new-file (with-out-str (print-fn new)))))

(defn- pad-notes [old new]
  (let [pad-to #(take (max 0 (- (count %1) (count %2))) (repeat {}))]
    [(into [] (concat old (pad-to new old)))
     (into [] (concat new (pad-to old new)))]))

(defn notes [old new & {:diff/keys [to-files
                                         to-repl
                                         timestamp]
                             :as spec}]
  (assert (or to-files to-repl) "Please pick an output option")
  (assert timestamp "Should be assoc'd to spec in scicloj.clay.v2.make/make!")
  (let [[old new] (pad-notes old new)
        note-diffs (mapv ddiff/diff old new)]
    (when (not-empty (ddiff/minimize note-diffs))
      (let [file-diff-print-fn (case to-files
                                 :deep-diff2/full ddiff/pretty-print
                                 :deep-diff2/minimal (comp ddiff/pretty-print
                                                           ddiff/minimize)
                                 ;; No diff, we write old/new for to-files anyways
                                 (:clojure/pprint nil) nil)
            print-fn pp/pprint
            repl-print-fn (case to-repl
                            :deep-diff2/full #(print-diffs ddiff/pretty-print
                                                           note-diffs)
                            :deep-diff2/minimal #(print-diffs (comp ddiff/pretty-print
                                                                    ddiff/minimize)
                                                              note-diffs)
                            :clojure/pprint #(doseq [[old' new'] (map vector old new)]
                                               (println "--------- old: ")
                                               (print-fn old')
                                               (println "--------- new: ")
                                               (print-fn new'))
                            nil nil)]
        (when to-files
          (write-diff-files old new note-diffs file-diff-print-fn print-fn spec))
        (when to-repl
          (repl-print-fn))))))
