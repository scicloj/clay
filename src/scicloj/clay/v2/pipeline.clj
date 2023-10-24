(ns scicloj.clay.v2.pipeline
  (:require [clojure.core.async :as async
             :refer [<! go go-loop timeout chan thread]]
            [scicloj.clay.v2.server :as server]
            [scicloj.clay.v2.show :as show]))

(defn new-pipeline []
  (let [events-channel         (async/chan 100)]
    (async/go-loop []
      (when-let [event (async/<! events-channel)]
        (show/show! event)
        (recur)))
    {:stop (fn []
             (async/close! events-channel))
     :process (fn [event]
                (async/>!! events-channel event))}))

(defonce *pipeline
  (atom nil))

(defn stop! []
  (server/close!)
  (when-let [s (:stop @*pipeline)]
    (s)))

(defn start! []
  (when-not (:stop @*pipeline)
    (server/open!)
    (reset! *pipeline
            (new-pipeline))))

(defn process! [event]
  (when-let [p (:process @*pipeline)]
    (p event)))

(defn handle-context! [context]
  (try
    (process! context)
    (catch Exception e
      (println [:error-in-clay-pipeline e]))))

(defn handle-form! [form]
  (handle-context!
   {:form form
    :value (eval form)}))

(defn handle-value! [value]
  (handle-context!
   {:value value}))
