(ns scicloj.clay.v1.util.resource
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
