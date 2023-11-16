(ns scicloj.clay.v2.show
  (:require
   [clojure.java.io :as io]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.page :as page]
   [scicloj.clay.v2.prepare :as prepare]
   [scicloj.clay.v2.server :as server]
   [scicloj.clay.v2.config :as config]
   [scicloj.clay.v2.util.path :as path]
   [scicloj.clay.v2.util.time :as time]))

(defn show-items!
  ([items]
   (show-items! items nil))
  ([items options]
   (server/update-page!
    (merge
     {:page (-> options
                (assoc :items items
                       :config (config/config))
                page/html)}
     (select-keys options [:html-path])))))


(defn show! [context]
  (-> context
      prepare/prepare-or-pprint
      vector
      show-items!))
