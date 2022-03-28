(ns scicloj.clay.v1.pipeline
  (:require [clojure.core.async :as async
             :refer [<! go go-loop timeout chan thread]]
            [scicloj.clay.v1.view :as view]))

(defn handle-value [{:keys [code value] :as event}]
  (view/show value code))

(defn handle [{:keys [event-type]
               :as event}]
  (some-> event-type
          (case :event-type/value (handle-value event))))

(defn new-pipeline [handler]
  (view/open)
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

(defn restart []
  (view/close)
  (when-let [s (:stop @*pipeline)]
    (s))
  (reset! *pipeline (new-pipeline #'handle)))

(defn start []
  (if-not (:stop @*pipeline)
    (restart)))

(defn process [event]
  (when-let [p (:process @*pipeline)]
    (p event)))
