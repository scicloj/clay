(ns scicloj.clay.v1.pipeline
  (:require [clojure.core.async :as async
             :refer [<! go go-loop timeout chan thread]]
            [scicloj.clay.v1.view :as view]))

(defn handle-value [{:keys [code value] :as event}
                    tools]
  (view/show! value code tools))

(defn create-handler [tools]
  (fn [{:keys [event-type]
        :as event}]
    (some-> event-type
            (case :event-type/value (handle-value event tools)))))

(defn new-pipeline [handler tools]
  (view/open! tools)
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

(defn restart! [{:keys [tools] :as config}]
  (view/setup! config)
  (view/close! tools)
  (when-let [s (:stop @*pipeline)]
    (s))
  (reset! *pipeline (new-pipeline (create-handler tools)
                                  tools)))

(defn start! [config]
  (if (:stop @*pipeline) ; already started, so just re-setup
    (view/setup! config)
    ;; actually start it
    (restart! config)))

(defn process! [event]
  (when-let [p (:process @*pipeline)]
    (p event)))
