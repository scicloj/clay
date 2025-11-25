(ns scicloj.clay.v2.read
  (:require [scicloj.read-kinds.notes :as notes]
            [scicloj.read-kinds.read :as read]
            [nrepl.core :as nrepl]))

;; TODO: not sure if generation is necessary???

(def *generation (atom 0))

(defn generation []
  (swap! *generation inc)
  @*generation)

;; for finding the ns only
(defn read-forms [code]
  (->> code
       clojure.tools.reader.reader-types/source-logging-push-back-reader
       repeat
       (map #(clojure.tools.reader/read % false ::EOF))
       (take-while (partial not= ::EOF))))

(defn read-ns-form [code]
  (->> code
       read-forms
       (filter (fn [form]
                 (and (sequential? form)
                      (-> form first (= 'ns)))))
       first))

;; TODO eval via nrepl for clay clj + clay dialect
(defn babashka-eval-capture [{:as spec
                              :keys [code
                                     babashka-nrepl-host
                                     babashka-nrepl-port]}]
  (with-open [^nrepl.transport.FnTransport
              conn (nrepl/connect :host babashka-nrepl-host
                                  :port babashka-nrepl-port)]
    (assoc spec :value
           (-> (nrepl/client conn 1000)    ; message receive timeout required
               (nrepl/message {:op "eval"
                               :code (cond form (pr-str form)
                                           code code)})
               doall))))

(defn ->notes [{:keys [single-form
                       single-value
                       code]}]
  ;; TODO infer eval via nrepl from set of :clojure-dialect
  (->> (cond single-value (conj (when code
                                  [{:form (read/read-ns-form code)}])
                                {:value single-value})
             single-form (conj (when code
                                 [{:form (read/read-ns-form code)}])
                               {:form single-form})
             :else code)
       (read/read-string-all)
       (into [] notes/notebook-xform)))

;; TODO: Not needed? read-kinds has a safe-notes wrapper already...
(defn ->safe-notes [code]
  (try
    (->notes code)
    (catch Exception e
      (println :invalid-notes (-> e
                                  Throwable->map
                                  (select-keys [:cause :data])))
      nil)))
