(ns scicloj.clay.v2.pipeline
  (:require [clojure.core.async :as async
             :refer [<! go go-loop timeout chan thread]]
            [scicloj.clay.v2.view :as view]))

(defn handle-value [{:keys [form value] :as event}
                    tools]
  (view/show! event tools))

(defn create-handler [tools]
  (fn [{:keys [event-type]
        :as event}]
    (some-> event-type
            (case :event-type/value (handle-value event tools)))))

(defn new-pipeline [handler tools events-source]
  (view/open! tools)
  (let [events-channel         (async/chan 100)]
    (async/go-loop []
      (when-let [event (async/<! events-channel)]
        (handler event)
        (recur)))
    {:stop (fn []
             (async/close! events-channel))
     :process (fn [event]
                (when (-> event :source (= events-source))
                  (async/>!! events-channel event)))}))

(defonce *pipeline
  (atom nil))

(defn restart! [{:keys [tools events-source]
                 :as config
                 :or {events-source :tap}}]
  (view/setup! config)
  (view/close! tools)
  (when-let [s (:stop @*pipeline)]
    (s))
  (reset! *pipeline (new-pipeline (create-handler tools)
                                  tools
                                  events-source)))

(defn start! [config]
  (if (:stop @*pipeline) ; already started, so just re-setup
    (view/setup! config)
    ;; actually start it
    (restart! config)))

(defn process! [event]
  (when-let [p (:process @*pipeline)]
    (p event)))


(defn handle-tap [{:keys [clay-tap? code-file form value]
                   :as dbg}]
  (when clay-tap?
    (try
      (process!
       {:event-type :event-type/value
        :code (some-> code-file slurp)
        :form form
        :value value
        :source :tap})
      (catch Exception e
        (println [:error-in-clay-pipeline e])))))

(add-tap #'handle-tap)
