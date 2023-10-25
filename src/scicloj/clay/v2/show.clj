(ns scicloj.clay.v2.show
  (:require
   [clojure.java.io :as io]
   [scicloj.clay.v2.page :as page]
   [scicloj.clay.v2.util.path :as path]
   [scicloj.clay.v2.prepare :as prepare]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.server :as server]
   [scicloj.clay.v2.state :as state]
   [scicloj.clay.v2.util.time :as time]))

(defn show-items!
  ([items]
   (show-items! items nil))
  ([items options]
   (state/swap-state-and-increment!
    (fn [state]
      (let [state1 (-> state
                       (assoc :html-path nil)
                       (assoc :date (java.util.Date.))
                       (merge options))
            page (-> state1
                     (assoc :items items)
                     page/html)]
        (-> state1
            (assoc :page page)))))
   (server/broadcast! "refresh")
   [:ok]))



(defn show! [context]
  (-> context
      prepare/prepare-or-pprint
      vector
      show-items!))

(defn write-html!
  ([]
   (write-html! (path/ns->target-path "docs/" *ns* ".html")))
  ([path]
   (io/make-parents path)
   (->> (state/page)
        (spit path))
   (println [:wrote path (time/now)])
   [:wrote path]))
