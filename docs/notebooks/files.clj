(ns files
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.clay.v2.api :as clay]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]))

(def *files (atom {}))

(defn ns-base-path []
  (str ".files/" *ns* "/"))

(defn init-ns-files! []
  (swap! *files assoc *ns* #{})
  (shell/sh "rm" "-rf" (ns-base-path)))

io/delete-file

(init-ns-files!)

(defn ns-files []
  (@*files *ns*))

(defn add-file! [source-path]
  (let [target-path (str (ns-base-path)
                         source-path)]
    (io/make-parents target-path)
    (io/copy (io/file source-path)
             (io/file target-path))
    (swap! *files
           update *ns*
           conj target-path)
    target-path))

(kind/hiccup
 [:img {:src (add-file! "resources/Clay.svg.png")}])
