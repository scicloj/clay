(ns scicloj.clay.v2.actions
  (:require
   [scicloj.clay.v2.notebook :as notebook]
   [scicloj.clay.v2.util.path :as path]
   [scicloj.clay.v2.server :as server]
   [scicloj.clay.v2.show :as show]
   [scicloj.clay.v2.quarto :as quarto]
   [scicloj.clay.v2.item :as item]))

(defn show-doc!
  ([path]
   (show-doc! path nil))
  ([path {:keys [title toc? custom-message]
          :as options}]
   (show/show-items!
    [(item/hiccup
      (or custom-message
          [:div
           [:p "showing document for "
            [:code (path/path->filename path)]]
           [:div.loader]]))])
   (let [doc (notebook/notebook-items path options)]
     (-> doc
         (show/show-items!
          {:title title
           :toc? toc?})))
   :ok))

(defn show-doc-and-write-html!
  [path options]
  (-> options
      (assoc :custom-message [:div
                              [:p "showing document for "
                               [:code (path/path->filename path)]]
                              [:p "and then writing as html file"]
                              [:div.loader]]
             :path path)
      (->> (show-doc! path)))
  (Thread/sleep 1000)
  #_(show/write-html!))

(defn render-quarto!
  [path {:keys [format title]
         :or {format :html}
         :as options}]
  (show/show-items!
   [(item/hiccup
     [:div
      [:p "generating Quarto document for "
       [:code (path/path->filename path)]]
      [:div.loader]])])
  (-> options
      (assoc
       :title (or title path)
       :path path)
      (->> (notebook/notebook-items path))
      (quarto/render-quarto! {:format format})))


(defn write-quarto!
  [path {:keys [title]
         :as options}]
  (-> options
      (assoc
       :title (or title path)
       :path path)
      (->> (notebook/notebook-items path))
      quarto/write-quarto!))

(defn handle-context! [context]
  (try
    (show/show! context)
    (catch Exception e
      (println [:error-in-clay-pipeline e]))))

(defn handle-form! [form]
  (handle-context!
   {:form form
    :value (eval form)}))

(defn handle-value! [value]
  (handle-context!
   {:value value}))
