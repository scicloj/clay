(ns scicloj.clay.v2.pipeline
  (:require [clojure.core.async :as async
             :refer [<! go go-loop timeout chan thread]]
            [scicloj.clay.v2.server :as server]))

(defn new-pipeline [handler]
  (let [events-channel         (async/chan 100)]
    (async/go-loop []
      (when-let [event (async/<! events-channel)]
        (handler event)
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
            (new-pipeline (fn [{:keys [event-type]
                                :as event}]
                            (some-> event-type
                                    (case :event-type/value (server/show! event))))))))

(defn process! [event]
  (when-let [p (:process @*pipeline)]
    (p event)))

(defn handle-form! [form]
  (try
    (process!
     {:event-type :event-type/value
      :form form
      :value (eval form)})
    (catch Exception e
      (println [:error-in-clay-pipeline e]))))

(defn handle-value! [value]
  (try
    (process!
     {:event-type :event-type/value
      :value value})
    (catch Exception e
      (println [:error-in-clay-pipeline e]))))
