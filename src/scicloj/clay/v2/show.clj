(ns scicloj.clay.v2.show
  (:require [scicloj.clay.v2.state :as state]
            [scicloj.clay.v2.server :as server]
            [scicloj.clay.v2.prepare :as prepare]
            [scicloj.clay.v2.page :as page]
            [scicloj.clay.v2.path :as path]
            [scicloj.clay.v2.time :as time]
            [clojure.java.io :as io]))

(defn show-items!
  ([items]
   (show-items! items nil))
  ([items options]
   (state/swap-state-and-increment!
    (fn [state]
      (-> state
          (assoc :quarto-html-path nil)
          (assoc :date (java.util.Date.))
          (assoc :items items)
          (merge options))))
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
   (->> @state/*state
        page/html
        (spit path))
   (println [:wrote path (time/now)])
   [:wrote path]))

(defn show-message! [hiccup]
  (state/set-items! [hiccup])
  (state/reset-quarto-html-path! nil)
  (server/broadcast! "refresh"))
