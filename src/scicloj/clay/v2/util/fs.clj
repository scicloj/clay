(ns scicloj.clay.v2.util.fs
  (:require [babashka.fs])
  (:import [java.nio.file CopyOption
            Files
            StandardCopyOption
            LinkOption
            Path]))

;; Adapting some code from babashka.fs.

(defn- ->copy-opts ^"[Ljava.nio.file.CopyOption;"
  [replace-existing copy-attributes atomic-move nofollow-links]
  (into-array CopyOption
              (cond-> []
                replace-existing (conj StandardCopyOption/REPLACE_EXISTING)
                copy-attributes  (conj StandardCopyOption/COPY_ATTRIBUTES)
                atomic-move      (conj StandardCopyOption/ATOMIC_MOVE)
                nofollow-links   (conj LinkOption/NOFOLLOW_LINKS))))

(defn- ->link-opts ^"[Ljava.nio.file.LinkOption;"
  [nofollow-links]
  (into-array LinkOption
              (cond-> []
                nofollow-links
                (conj LinkOption/NOFOLLOW_LINKS))))

(defn copy-tree-no-clj
  ([src dest]
   (let [replace-existing true
         copy-attributes true
         nofollow-links false
         copy-options (->copy-opts replace-existing copy-attributes false nofollow-links)
         link-options (->link-opts nofollow-links)
         from (babashka.fs/real-path src {:nofollow-links nofollow-links})
         ;; using canonicalize here because real-path requires the path to exist
         to (babashka.fs/canonicalize dest {:nofollow-links nofollow-links})]
     (babashka.fs/walk-file-tree
      from {:pre-visit-dir (fn [dir _attrs]
                             (let [rel (babashka.fs/relativize from dir)
                                   to-dir (babashka.fs/path to rel)]
                               (when-not (Files/exists to-dir link-options)
                                 (Files/copy ^Path dir to-dir
                                             ^"[Ljava.nio.file.CopyOption;"
                                             copy-options)))
                             :continue)
            :visit-file (fn [from-path _attrs]
                          (when (->> from-path
                                     babashka.fs/extension
                                     (#{"clj" "cljc" "cljs"})
                                     not)
                            (let [rel (babashka.fs/relativize from from-path)
                                  to-file (babashka.fs/path to rel)]
                              (Files/copy ^Path from-path to-file
                                          ^"[Ljava.nio.file.CopyOption;"
                                          copy-options)
                              :continue))
                          :continue)}))))
