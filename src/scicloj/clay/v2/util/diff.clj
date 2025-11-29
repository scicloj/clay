(ns scicloj.clay.v2.util.diff
  (:require [clojure.string :as str]
            [clojure.pprint :as pp]
            [babashka.fs :as fs]
            [lambdaisland.deep-diff2 :as ddiff]))

(defn- print-diffs [diff-print-fn note-diffs]
  (doseq [diff note-diffs]
    (when (some-> diff ddiff/minimize not-empty)
      (diff-print-fn diff))))

(defn- diff-print-fn [k]
  (case k
    :deep-diff2/full ddiff/pretty-print
    :deep-diff2/minimal (comp ddiff/pretty-print
                              ddiff/minimize)
    nil))

(defn- write-diff-files [old new note-diffs diff-print-fn print-fn
                         {:diff/keys [keep-dirs
                                      timestamp]
                          :keys [full-source-path]
                          :as spec}]
  (let [diffs-base-path (fs/absolutize "read-kinds-diffs")
        source-name (-> full-source-path (str/replace "/" "."))
        diffs-path (-> diffs-base-path
                       (fs/path (str source-name "~" timestamp)))
        diff-base-file (->> source-name
                            (fs/path diffs-path)
                            str)
        no-diff-file (str diff-base-file ".no-diff")
        diff-file (str diff-base-file ".diff.edn")
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
    (if (some not-empty (ddiff/minimize note-diffs))
      (when diff-print-fn
        (println "Clay:  Creating diff file" diff-file "& old/new")
        (spit diff-file (with-out-str
                          (print-diffs diff-print-fn note-diffs))))
      (do (println "Clay:  Creating no-diff file" no-diff-file "& old/new")
          (spit no-diff-file "no difference")))
    (spit old-file (with-out-str (print-fn old)))
    (spit new-file (with-out-str (print-fn new)))
    (println "Clay:  Copying latest diff files to" (str diffs-base-path))
    (doseq [file (fs/list-dir diffs-base-path
                              #(fs/regular-file? % {:nofollow-links true}))]
      (fs/delete file))
    (doseq [file (fs/list-dir diffs-path
                              #(fs/regular-file? % {:nofollow-links true}))]
      (fs/copy file diffs-base-path))))

(defn- pad-notes [old new]
  (let [pad-to #(take (max 0 (- (count %1) (count %2))) (repeat {}))]
    [(into [] (concat old (pad-to new old)))
     (into [] (concat new (pad-to old new)))]))

;; TODO clean up
(def only-eq-kinds
  #{:kind/fn
    :kind/table
    :kind/dataset})

(defn- only-eq-kind-value [item]
  (when (only-eq-kinds (:kind item))
    (:value item)))

(defn- prep-notes [old new]
  [(mapv (fn [old* new*]
           (let [old-v (only-eq-kind-value old*)
                 new-v (only-eq-kind-value new*)
                 equal-v? (= old-v new-v)]
             (cond-> old* old-v (assoc :value [(:kind old*) :value :eq equal-v?]))))
         old new)
   (mapv (fn [old* new*]
           (let [old-v (only-eq-kind-value old*)
                 new-v (only-eq-kind-value new*)
                 equal-v? (= old-v new-v)]
             (cond-> new* new-v (assoc :value [(:kind new*) :value :eq equal-v?]))))
         old new)])

(defn notes [old new & {:diff/keys [to-files
                                    to-repl
                                    timestamp]
                        :as spec}]
  (assert (or to-files to-repl) "Please pick an output option")
  (assert timestamp "Should be assoc'd to spec in scicloj.clay.v2.make/make!")
  (let [[old new] (pad-notes old new)
        [old new] (prep-notes old new)
        note-diffs (mapv ddiff/diff old new)
        file-diff-print-fn (case to-files
                             (:deep-diff2/full :deep-diff2/minimal)
                             (diff-print-fn to-files)
                             ;; No diff, but we always write old/new
                             ;; when to-files is specified
                             (:clojure/pprint nil) nil)
        print-fn pp/pprint
        repl-print-fn (case to-repl
                        (:deep-diff2/full :deep-diff2/minimal)
                        #(print-diffs (diff-print-fn to-repl) note-diffs)
                        :clojure/pprint #(doseq [[old* new*] (map vector old new)]
                                           (println "--------- old: ")
                                           (print-fn old*)
                                           (println "--------- new: ")
                                           (print-fn new*))
                        nil nil)]
    (when to-repl
      (println "Clay:  Notes diff start")
      (let [diff (some not-empty (ddiff/minimize note-diffs))]
        (when-not diff
          (println "Clay:  >>>> No difference!"))
        (repl-print-fn)
        ;; Add note on no difference at the end too when printing all data
        (when (and (= to-repl :clojure/pprint) (not diff))
          (println "Clay:  >>>> No difference!")))
      (println "Clay:  Notes diff end"))
    (when to-files
      (write-diff-files old new note-diffs file-diff-print-fn print-fn spec))))
