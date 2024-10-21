(ns scicloj.clay.v2.read
  (:require [clojure.tools.reader]
            [clojure.tools.reader.reader-types]
            [parcera.core :as parcera]
            [clojure.string :as str]))

(def *generation (atom 0))

(defn generation []
  (swap! *generation inc)
  @*generation)

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

(defn read-by-tools-reader [code]
  (-> code
      ;; avoiding a tools.reader bug -- see:
      ;; https://github.com/scicloj/clay/issues/151#issuecomment-2373488031
      (str/replace #"\r\n" "\n")
      (->> read-forms
           (map (fn [form]
                  (let [{:keys [line column
                                end-line end-column
                                code]}
                        (meta form)]
                    (when line ; skip forms with no location info
                      {:method :tools-reader
                       :region [line column
                                end-line end-column]
                       :code (-> form meta :source)
                       :form form}))))
           (filter some?))))

(defn read-by-parcera [code]
  (->> code
       parcera/ast
       rest
       (map (fn [node]
              (let [node-type     (first node)
                    node-contents (rest node)]
                ;; We use parcera only for specific types of
                ;; code blocks, that tools.reader does not
                ;; provide location info for.
                (some->> (when (#{:number :string :symbol :keyword :comment}
                                node-type)
                           {:code (first node-contents)})
                         (merge {:method :parcera
                                 :region (->> node
                                              meta
                                              ((juxt :parcera.core/start
                                                     :parcera.core/end))
                                              (mapcat (juxt :row
                                                            (comp inc
                                                                  :column)))
                                              vec)}
                                (when (= :comment node-type)
                                  {:comment? true}))))))
       (filter some?)))

(defn unified-cleaned-comment-block [comment-blocks-sorted-by-region]
  {:region  (vec (concat (->> comment-blocks-sorted-by-region
                              first
                              :region
                              (take 2))
                         (->> comment-blocks-sorted-by-region
                              last
                              :region
                              (drop 2))))
   :code (->> comment-blocks-sorted-by-region
              (reduce (fn [{:keys [generated-string max-line]}
                           {:keys [region code]}]
                        {:generated-string (str generated-string
                                                (apply str (-> region
                                                               first
                                                               (- max-line)
                                                               (repeat "\n")))
                                                code)
                         :max-line (-> region
                                       (nth 2)
                                       (max max-line))})
                      {:generated-string ""
                       :max-line (->> comment-blocks-sorted-by-region
                                      first
                                      :region
                                      first)})
              :generated-string)
   :comment? true})

(defn ->notes [code]
  (->> code
       ((juxt read-by-tools-reader read-by-parcera))
       (apply concat)
       (group-by :region)
       (map (fn [[region results]]
              (if (-> results count (= 1))
                (first results)
                ;; prefer tools.reader over parcera
                (->> results
                     (filter #(-> % :method (= :tools-reader)))
                     first))))
       (sort-by :region)
       (map #(dissoc % :method))
       (partition-by :comment?)
       (mapcat (fn [part]
                 (if (-> part first :comment?)
                   [(unified-cleaned-comment-block part)]
                   part)))
       (mapv (let [g (generation)]
               (fn [note-data]
                 (-> note-data
                     (assoc :gen g)))))))


(defn ->safe-notes [code]
  (try
    (->notes code)
    (catch Exception e
      (println :invalid-notes (-> e
                                  Throwable->map
                                  (select-keys [:cause :data])))
      nil)))
