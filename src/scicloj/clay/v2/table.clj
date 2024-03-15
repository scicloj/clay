(ns scicloj.clay.v2.table)

(defn row-vectors->table-hiccup [column-names row-vectors]
  (->> [:table {:class "table table-hover table-responsive"}
        (when column-names
          [:thead
           (->> column-names
                (mapv (fn [x] [:th x]))
                (into [:tr]))])
        (->> row-vectors
             (map (fn [row]
                    (->> row
                         (mapv (fn [x] [:td x]))
                         (into [:tr]))))
             vec
             (into [:tbody]))]
       (filter some?)
       vec))

(defn row-maps->table-hiccup
  ([row-maps]
   (-> (mapcat keys row-maps) ;; ensure we cover every possibly missing name
       (distinct)
       (row-maps->table-hiccup row-maps)))
  ([column-names row-maps]
   (if column-names
     (->> row-maps
          (mapv (fn [row]          ; Actually row can be a record, too.
                  (mapv #(get row %) column-names)))
          (row-vectors->table-hiccup column-names))
     ;; else
     (row-maps->table-hiccup row-maps))))

(defn dataset->table-hiccup [dataset]
  (->> dataset
       vals
       (apply map vector)
       (row-vectors->table-hiccup (keys dataset))))

(defn dataset?
  [dataset-or-data]
  (-> dataset-or-data
      class
      str
      (= "class tech.v3.dataset.impl.dataset.Dataset")))

(defn seq-of-maps?
  [dataset-or-data]
  (and (sequential? dataset-or-data)
       (every? map? dataset-or-data)))

(defn seq-of-seqs?
  [dataset-or-data]
  (and (sequential? dataset-or-data)
       (every? sequential? dataset-or-data)))

(defn ->table-hiccup [dataset-or-data]
  (cond
    (dataset? dataset-or-data) (dataset->table-hiccup dataset-or-data)
    (seq-of-seqs? dataset-or-data) (row-vectors->table-hiccup nil dataset-or-data)
    (seq-of-maps? dataset-or-data) (row-maps->table-hiccup dataset-or-data)
    :else (let [{:keys [row-maps row-vectors column-names]} dataset-or-data]
            (cond
              ;; row-vectors keys exists
              row-vectors (row-vectors->table-hiccup column-names row-vectors)
              ;; row-maps keys exists
              row-maps (row-maps->table-hiccup column-names row-maps)
              ;; treat any other cases as map of seqs
              :else (dataset->table-hiccup dataset-or-data)))))
