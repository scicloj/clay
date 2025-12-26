(ns scicloj.clay.v2.old.util.resource
  (:refer-clojure :exclude [get])
  (:require [scicloj.tempfiles.api :as tempfiles]))

(def cached-resource
  (memoize
   (fn [url]
     (let [path (-> ".cache"
                    tempfiles/tempfile!
                    :path)]
       (->> url
            slurp
            (spit path))
       path))))

(defn get [url]
  (-> url
      cached-resource
      slurp))
