(ns scicloj.clay.v2.nrepl
  (:require [nrepl.core :as nrepl]
            [nrepl.middleware :as middleware]
            [nrepl.middleware.print :as print]
            [nrepl.transport :as transport]
            [scicloj.clay.v2.pipeline :as pipeline]))


(defn handle-message [{:keys [id op] :as request}
                      {:keys [value err] :as message}]
  ;; disabled in this version
  (when (and (= "eval" op)
             (->> request
                  :code
                  (re-find #"clojure.lang.Compiler/load")
                  not))
    (cond
      ;;
      (contains? message :value)
      (pipeline/process!
       {:request-id id
        :value      value
        :code (:code request)
        :event-type :event-type/value
        :source :nrepl}))))


(defn middleware [f]
  (fn [request]
    (-> request
        (update :transport (fn [t]
                             (reify transport/Transport
                               (recv [req]
                                 (transport/recv t))
                               (recv [req timeout]
                                 (transport/recv t timeout))
                               (send [this message]
                                 (handle-message request message)
                                 (transport/send t message)
                                 this))))
        (f))))

(middleware/set-descriptor! #'middleware
                            {:requires #{#'print/wrap-print}
                             :expects #{"eval"}
                             :handles {}})
