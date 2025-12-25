(ns scicloj.clay.v2.old.util.fs
  (:require [babashka.fs :as fs]
            [clojure.string :as str]))

(defn copy-tree-no-clj [src dest]
  (let [from (fs/real-path src {:nofollow-links false})
        to (fs/canonicalize dest {:nofollow-links false})]
    (fs/walk-file-tree from
                       {:visit-file (fn [from-path _attrs]
                                      (when (not= "clj" (fs/extension from-path))
                                        (let [rel (fs/relativize from from-path)
                                              to-file (fs/path to rel)]
                                          (fs/create-dirs (fs/parent to-file))
                                          (fs/copy from-path to-file {:replace-existing true :copy-attributes true})))
                                      :continue)})))

(defn child? [parent-path child-path]
  (-> (str/starts-with? (str (fs/canonicalize child-path))
                        (str (fs/canonicalize parent-path)))))

(defn find-notebooks [base-source-path]
  (->> (fs/glob base-source-path "**.clj")
       (map #(fs/relativize base-source-path %))
       (map str)))
