(ns scicloj.clay.v2.page
  (:require
   [clj-yaml.core :as yaml]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [hiccup.core :as hiccup]
   [hiccup.page]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.prepare :as prepare]
   [scicloj.clay.v2.styles :as styles]
   [scicloj.clay.v2.util.portal :as portal]
   [scicloj.clay.v2.util.resource :as resource]
   [scicloj.clay.v2.files :as files]))

(def special-lib-resources
  {:vega {:js {:from-local-copy
               ["https://cdn.jsdelivr.net/npm/vega@5.22.1"
                "https://cdn.jsdelivr.net/npm/vega-lite@5.6.0"
                "https://cdn.jsdelivr.net/npm/vega-embed@6.21.0"]}}
   :datatables {:js {:from-the-web
                     ["https://cdn.datatables.net/1.13.6/js/jquery.dataTables.min.js"]}
                :css {:from-the-web
                      ["https://cdn.datatables.net/1.13.6/css/jquery.dataTables.min.css"]}}
   :echarts {:js {:from-local-copy
                  ["https://cdn.jsdelivr.net/npm/echarts@5.4.1/dist/echarts.min.js"]}}
   :cytoscape {:js {:from-local-copy
                    ["https://cdnjs.cloudflare.com/ajax/libs/cytoscape/3.23.0/cytoscape.min.js"]}}
   :plotly {:js {:from-local-copy
                 ["https://cdnjs.cloudflare.com/ajax/libs/plotly.js/2.20.0/plotly.min.js"]}}
   :katex {:js {:from-local-copy
                ["https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"]}
           :css {:from-the-web
                 ;; fetching the KaTeX css from the web
                 ;; to avoid fetching the fonts locally,
                 ;; which would need a bit more care
                 ;; (see https://katex.org/docs/font.html)
                 ["https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css"]}}
   :three-d-mol {:js {:from-the-web
                      ["https://cdnjs.cloudflare.com/ajax/libs/3Dmol/1.5.3/3Dmol.min.js"]}}
   :leaflet {;; fetching Leaflet from the web
             ;; to avoid fetching the images locally,
             ;; which would need a bit more care.
             :js {:from-the-web
                  ["https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"]}
             :css {:from-the-web
                   ["https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"]}}
   :reagent {:js {:from-local-copy
                  ["https://unpkg.com/react@18/umd/react.production.min.js"
                   "https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"
                   "https://scicloj.github.io/scittle/js/scittle.js"
                   "https://scicloj.github.io/scittle/js/scittle.cljs-ajax.js"
                   "https://scicloj.github.io/scittle/js/scittle.reagent.js"
                   "https://cdn.jsdelivr.net/npm/d3-require@1"]}}
   :tmdjs {:js {:from-local-copy
                ["https://scicloj.github.io/scittle/js/scittle.tmdjs.js"]}}
   :emmy {:js {:from-local-copy
               ["https://scicloj.github.io/scittle/js/scittle.emmy.js"]}}
   :mathbox {:js {:from-local-copy
                  ["https://scicloj.github.io/scittle/js/scittle.mathbox.js"]}}
   :portal {:js {:from-local-copy [portal/url]}}
   :html-default {:js {:from-local-copy
                       ["https://code.jquery.com/jquery-3.6.0.min.js"
                        "https://code.jquery.com/ui/1.13.1/jquery-ui.min.js"
                        "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"]}}
   :md-default {:js {:from-local-copy
                     ["https://code.jquery.com/jquery-3.6.0.min.js"
                      "https://code.jquery.com/ui/1.13.1/jquery-ui.min.js"]}}})

(def include
  {:js hiccup.page/include-js
   :css hiccup.page/include-css})

(defn include-from-a-local-file [url custom-name js-or-css
                                 {:keys [full-target-path base-target-path]}]
  (let [path (files/next-file!
              full-target-path
              custom-name
              url
              (str "." (name js-or-css)))]
    (->> url
         slurp
         (spit path))
    (-> path
        (string/replace
         (re-pattern (str "^"
                          base-target-path
                          "/"))
         "")
        ((include js-or-css)))))

(defn include-libs [spec libs]
  (->> [:js :css]
       (mapcat (fn [js-or-css]
                 (->> libs
                      (mapcat
                       (fn [lib]
                         (->> lib
                              special-lib-resources
                              js-or-css
                              ((fn [{:keys [from-the-web from-local-copy]}]
                                 (concat
                                  (some->> from-the-web
                                           (apply (include js-or-css))
                                           vector)
                                  (some->> from-local-copy
                                           (map (fn [url]
                                                  (include-from-a-local-file
                                                   url
                                                   lib
                                                   js-or-css
                                                   spec)))))))))))))
       (apply concat)
       hiccup/html
       (format "\n%s\n")))

(def font-links
  " <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">
    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>
    <link href=\"https://fonts.googleapis.com/css2?family=Roboto&display=swap\" rel=\"stylesheet\">
")


(defn html [{:as spec
             :keys [items title toc?]}]
  (let [special-libs (->> items
                          (mapcat :deps)
                          distinct
                          (cons :html-default))
        head [:head
              [:meta {:charset "UTF-8"}]
              [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
              [:link {:rel "icon" :href "data:,"}] ; avoid favicon.ico request: https://stackoverflow.com/a/38917888
              #_[:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css"}]
              font-links
              [:style (styles/main :table)]
              [:style (styles/main :loader)]
              #_[:style (styles/main :code)]
              [:style (styles/highlight :qtcreator-light)]
              (include-from-a-local-file
               "https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/css/bootstrap.min.css"
               "bootstrap"
               :css
               spec)
              (when toc?
                (include-from-a-local-file
                 "https://cdn.rawgit.com/afeld/bootstrap-toc/v1.0.1/dist/bootstrap-toc.min.css"
                 "bootstrap-toc"
                 :css
                 spec))
              (when toc?
                [:style (styles/main :bootstrap-toc-customization)])
              (->> special-libs
                   (mapcat (comp :from-the-web :css special-lib-resources))
                   distinct
                   (map #(-> %
                             hiccup.page/include-css
                             hiccup/html))
                   (string/join "\n"))
              (->> special-libs
                   (mapcat (fn [lib]
                             (->> lib
                                  special-lib-resources
                                  :css
                                  :from-local-copy
                                  (map (fn [url]
                                         (include-from-a-local-file
                                          url
                                          lib
                                          :css
                                          spec)))))))
              [:title (or title "Clay")]]
        body [:body  {:style {:background "#fcfcfc"
                              :font-family "'Roboto', sans-serif"
                              :width "90%"
                              :margin "auto"}
                      :data-spy "scroll"
                      :data-target "#toc"}
              (when toc?
                (include-from-a-local-file
                 "https://cdn.rawgit.com/afeld/bootstrap-toc/v1.0.1/dist/bootstrap-toc.min.js"
                 "bootstrap-toc"
                 :js
                 spec))
              [:script {:type "text/javascript"}
               (-> "highlight/highlight.min.js"
                   io/resource
                   slurp)]
              (->> special-libs
                   (mapcat (fn [lib]
                             (->> lib
                                  special-lib-resources
                                  :js
                                  :from-local-copy
                                  (map (fn [url]
                                         (include-from-a-local-file
                                          url
                                          lib
                                          :js
                                          spec)))))))
              (->> special-libs
                   (mapcat (comp :from-the-web :js special-lib-resources))
                   distinct
                   (apply hiccup.page/include-js))
              [:div.container
               [:div.row
                (when toc?
                  [:div.col-sm-3
                   [:nav.sticky-top {:id "toc"
                                     :data-toggle "toc"}]])
                [:div {:class (if toc?
                                "col-sm-9"
                                "col-sm-12")}
                 [:div
                  (->> items
                       (map-indexed
                        (fn [i item]
                          [:div {:style {:margin "15px"}}
                           (prepare/item->hiccup item
                                                 {:id (str "item" i)})]))
                       (into [:div]))]]]]
              [:script {:type "text/javascript"}
               "hljs.highlightAll();"]]]
    (-> (hiccup.page/html5 head body)
        (string/replace #"<table>"
                        "<table class='table table-hover'>"))))




(defn md [{:as spec
           :keys [items title quarto]}]
  (str
   (->> quarto
        yaml/generate-string
        (format "\n---\n%s\n---\n"))
   ;; " "
   (hiccup/html
    [:style (styles/main :table)]
    [:style (styles/main :md-main)])
   (->> items
        (mapcat :deps)
        distinct
        (cons :md-default)
        (include-libs spec))
   (->> items
        (map-indexed
         (fn [i item]
           (prepare/item->md item
                             {:id (str "item" i)})))
        (string/join "\n\n"))))
