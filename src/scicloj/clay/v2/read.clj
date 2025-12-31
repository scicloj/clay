(ns scicloj.clay.v2.read
  (:require [scicloj.read-kinds.notes :as notes]
            [scicloj.read-kinds.read :as read]
            [clojure.tools.reader]
            [clojure.tools.reader.reader-types]
            [clojure.string :as str]))

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

;; TODO this is intentionally not a complete replacement
;;      for read-kinds.notes/notebook-xform, it doesn't
;;      assoc :kind/md so notebook-xform can still look
;;      for expected kinds in the pipeline/transform
(defn collapse-comments-ws [collapse-comments-ws? notes]
  (if collapse-comments-ws?
    (let [collapse (comp #{:kind/whitespace :kind/comment} :kind)
          comment? (comp #{:kind/comment} :kind)]
      (->> notes
           (partition-by (comp boolean collapse))
           (mapcat
            (fn [notes*]
              (if (some comment? notes*)
                ;; TODO Pulling in all comments and whitespace
                ;; This is only done to easily get equality with old comments
                [{:value (let [comment* (->> notes*
                                             (map #(get % :value (:code %)))
                                             str/join)]
                           (-> comment*
                               (str/replace #"^\s+" "")
                               (str/trim-newline)))
                  :kind :kind/comment}]
                notes*)))))
    notes))

;; TODO keep this or something like it
(defn ->notes [{:keys [single-form
                       single-value
                       code
                       collapse-comments-ws?]}]
  (cond single-value (conj (when code
                             [{:form (read-ns-form code)}])
                           {:value single-value})
        ;; TODO Doesn't actually eval the form
        single-form (conj (when code
                            [{:form (read-ns-form code)}])
                          {:form single-form})
        :else (->> code
                   (read/read-string-all)
                   (read/eval-notes)
                   (collapse-comments-ws collapse-comments-ws?)
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
