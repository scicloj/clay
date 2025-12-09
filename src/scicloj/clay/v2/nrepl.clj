(ns scicloj.clay.v2.nrepl
  (:require [nrepl.core :as nrepl]))

(declare close-connection)

(defonce nrepl-connection
  (atom {:connection nil
         :session nil}))

(defn connect [nrepl-connection & {:keys [host port] :as options}]
  (when (:connection @nrepl-connection)
    ;; TODO error handling
    (close-connection nrepl-connection))
  (let [connection (nrepl/connect :host host :port port)
        client (nrepl/client connection 1000)
        _ (nrepl/message client {:op "describe"})
        session (nrepl/client-session client)]
    (reset! nrepl-connection {:connection connection
                              :session session})))

;; TODO eval via nrepl for clay clj + clay dialect
(defn eval-context [{:keys [form] :as context}
                    & {:keys [host port] :as options}]
  (when (not (:connection @nrepl-connection))
    (connect nrepl-connection :host host :port port))
  (let [response (-> (:session @nrepl-connection)
                     (nrepl/message {:op "eval"
                                     :code (pr-str form)})
                     (nrepl/combine-responses))]
    ;; TODO we only expect one value
    (update response :value #(read-string (first %)))))

(defn close-connection [nrepl-connection]
  (try
    (let [{:keys [connection session]} @nrepl-connection]
      ;; TODO can send close message, but is it ok for jank nrepl?
      (when session
        (doall (nrepl/message session {:op "close"})))
      (when connection
        (.close ^nrepl.transport.FnTransport
                connection)))
    (catch Exception e
      ;; TODO differentiate?
      (throw e))
    (finally
      ;; TODO do we want to make this more coherent? locking?
      (reset! nrepl-connection {:connection nil
                                :session nil}))))

(comment
  (close-connection nrepl-connection)

  (connect nrepl-connection :host "localhost" :port 4200)

  (eval-context {:form '(do (println "hello jank")
                            [1 2 3])}
                :host "localhost" :port 4200))
