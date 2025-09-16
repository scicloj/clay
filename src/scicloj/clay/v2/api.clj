(ns scicloj.clay.v2.api)
;; It can take some time to require dependencies,
;; we give the user some immediate feedback to let them know something is happening.
(println "Clay loading...")
(ns scicloj.clay.v2.api
  (:require [scicloj.clay.v2.make :as make]
            [scicloj.clay.v2.config :as config]
            [scicloj.clay.v2.server :as server]
            [scicloj.clay.v2.live-reload :as live-reload]
            [scicloj.kindly.v4.kind :as kind]))

(defn stop! []
  (server/close!)
  (live-reload/stop!)
  [:ok])

(defn welcome! []
  (make/make!
   {:single-value (kind/hiccup
                   [:div
                    [:p [:pre (str (java.util.Date.))]]
                    [:p [:pre [:a {:href "https://scicloj.github.io/clay/"}
                               "Clay"]
                         " is ready, waiting for interaction."]]])}))

(defn start!
  ([]
   (start! {:browse true}))
  ([{:as opts :keys [port browse]}]
   (server/open! opts)
   (welcome!)
   [:ok]))

(defn make! [spec]
  (make/make! spec))

(defn make-hiccup [spec]
  (-> spec
      (assoc :format [:hiccup]
             :show false)
      make/make!
      :info
      first first))

(defn browse! []
  (server/browse!))

(defn port []
  (server/port))

(defn url []
  (server/url))

(defn config
  "Gathers configuration from the default, a clay.edn, and a spec if provided"
  ([] (config/config))
  ([spec] (config/config spec)))
