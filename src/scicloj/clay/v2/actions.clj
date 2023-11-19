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
   (let [target-path (path/ns->target-path "docs/" *ns* ".html")
         doc (notebook/notebook-items path
                                      (assoc options
                                             :target-path target-path))]
     (-> doc
         (show/show-items!
          {:title title
           :toc? toc?
           :html-path target-path})))
   :ok))

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
  (let [target-path (path/ns->target-path "docs/" *ns* ".md")]
    (-> options
        (assoc
         :title (or title path)
         :path path
         :target-path target-path)
        (->> (notebook/notebook-items path))
        (quarto/render-quarto! {:format format
                                :md-path target-path}))))

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
