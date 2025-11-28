(ns scicloj.clay.v2.read
  (:require [scicloj.read-kinds.notes :as notes]
            [scicloj.read-kinds.read :as read]
            [clojure.tools.reader]
            [clojure.tools.reader.reader-types]))

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

(defn collapse-comments-ws [notes]
  (let [collapse (comp #{:kind/whitespace :kind/comment} :kind)
        comment? (comp #{:kind/comment} :kind)]
    (->> notes
         (partition-by (comp boolean collapse))
         (mapcat
          #(if (some collapse %)
             [(cond-> {:code (str/join (map :code %))
                       :kind (if (some comment? %)
                               :kind/comment
                               :kind/whitespace)}
                (some comment? %)
                ;; TODO does :value need :code from whitespace?
                (assoc :value (str/join (map :value %))))]
             %)))))

;; TODO keep this or something like it
(defn ->notes [{:keys [single-form
                       single-value
                       code]}]
  (cond single-value (conj (when code
                             [{:form (read-ns-form code)}])
                           {:value single-value})
        ;; TODO Doesn't actually eval the form
        single-form (conj (when code
                            [{:form (read-ns-form code)}])
                          {:form single-form})
        :else (->> code
                   (read/read-string-all)
                   ;; TODO maybe optional for diffing
                   collapse-comments-ws
                   (into [] notes/notebook-xform))))

;; TODO: Not needed? read-kinds has a safe-notes wrapper already...
(defn ->safe-notes [code]
  (try
    (->notes code)
    (catch Exception e
      (println :invalid-notes (-> e
                                  Throwable->map
                                  (select-keys [:cause :data])))
      nil)))
