(ns scicloj.clay.v2.table)

(defn row-vectors->table-hiccup [column-names row-vectors]
  [:table {:class "table table-hover table-responsive"}
   [:thead
    (->> column-names
         (mapv (fn [x] [:th (str x)]))
         (into [:tr]))]
   (->> row-vectors
        (map-indexed
         (fn [i row]
           (->> row
                (mapv (fn [x] [:td x #_(-> x
                                           println
                                           with-out-str)]))
                (into [:tr]))))
        vec
        (into [:tbody]))])

(defn row-maps->table-hiccup
  ([row-maps]
   (-> row-maps
       first
       keys
       (row-maps->table-hiccup row-maps)))
  ([column-names row-maps]
   (if column-names
     (->> row-maps
          (mapv (fn [row] ; Actually row can be a record, too.
                  (mapv #(get row %) column-names)))
          (row-vectors->table-hiccup column-names))
     ;; else
     (row-maps->table-hiccup row-maps))))

(defn dataset->table-hiccup [dataset]
  (->> dataset
       vals
       (apply map vector)
       (row-vectors->table-hiccup (keys dataset))))

(defn ->table-hiccup [dataset-or-options]
  (if (-> dataset-or-options
          class
          str
          (= "class tech.v3.dataset.impl.dataset.Dataset"))
    (dataset->table-hiccup dataset-or-options)
    (let [{:keys [row-maps row-vectors column-names]} dataset-or-options]
      (assert column-names)
      (if row-vectors
        (row-vectors->table-hiccup column-names row-vectors)
        (do
          (assert row-maps)
          (row-maps->table-hiccup column-names row-maps))))))
