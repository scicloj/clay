(ns scicloj.clay.v2.tool.scittle.page
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [hiccup.core :as hiccup]
            [hiccup.page]
            [scicloj.clay.v2.tool.scittle.cljs-generation :as cljs-generation]
            [scicloj.clay.v2.tool.scittle.widget :as widget]
            [scicloj.clay.v2.tool.scittle.styles :as styles]
            [scicloj.clay.v2.util.resource :as resource]
            [scicloj.clay.v2.html.table :as table]
            [clj-yaml.core :as yaml]
            [scicloj.clay.v2.tool.scittle.portal :as portal]))

(def special-libs-set
  #{'datatables 'vega 'echarts 'cytoscape 'plotly 'katex
    'three-d-mol 'three-d-mol-viewer 'leaflet})

(def special-lib-resources
  {'vega {:js {:from-local-copy
               ["https://cdn.jsdelivr.net/npm/vega@5.22.1"
                "https://cdn.jsdelivr.net/npm/vega-lite@5.6.0"
                "https://cdn.jsdelivr.net/npm/vega-embed@6.21.0"]}}
   'datatables {:js {:from-the-web
                     ["https://cdn.datatables.net/1.11.5/js/jquery.dataTables.min.js"]}
                :css {:from-the-web
                      ["https://cdn.datatables.net/1.11.5/css/jquery.dataTables.min.css"]}}
   'echarts {:js {:from-local-copy
                  ["https://cdn.jsdelivr.net/npm/echarts@5.4.1/dist/echarts.min.js"]}}
   'cytoscape {:js {:from-local-copy
                    ["https://cdnjs.cloudflare.com/ajax/libs/cytoscape/3.23.0/cytoscape.min.js"]}}
   'plotly {:js {:from-local-copy
                 ["https://cdnjs.cloudflare.com/ajax/libs/plotly.js/2.20.0/plotly.min.js"]}}
   'katex {:js {:from-local-copy
                ["https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"]}
           :css {:from-the-web
                 ;; fetching the KaTeX css from the web
                 ;; to avoid fetching the fonts locally,
                 ;; which would need a bit more care
                 ;; (see https://katex.org/docs/font.html)
                 ["https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css"]}}
   'three-d-mol {:js {:from-local-copy
                      ["https://cdnjs.cloudflare.com/ajax/libs/3Dmol/1.5.3/3Dmol.min.js"]}}
   'three-d-mol-viewer {:js {:from-local-copy
                             ["https://cdnjs.cloudflare.com/ajax/libs/3Dmol/1.5.3/3Dmol.min.js"]}}
   'leaflet {:js {:from-local-copy
                  ["https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"]}
             :css {:from-local-copy
                   ["https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"]}}})

(defn js-from-local-copies [& urls]
  (->> urls
       (map resource/get)
       (map (partial vector :script {:type "text/javascript"}))
       (into [:div])))

(defn css-from-local-copies [& urls]
  (->> urls
       (map resource/get)
       (map (partial vector :style))
       (into [:div])))

(defn special-libs-in-form [f]
  (->> f
       (tree-seq (fn [x]
                   (or (vector? x)
                       (map? x)
                       (list? x)
                       (seq? x)))
                 (fn [x]
                   (if (map? x)
                     (vals x)
                     x)))
       (filter special-libs-set)
       distinct
       set))

(defn page [{:keys [widgets data port title toc? counter]}]
  (let [special-libs (->> widgets
                          special-libs-in-form)]
    (when-not port
      (throw (ex-info "missing port" {})))
    (-> (hiccup.page/html5 [:head
                            [:meta {:charset "UTF-8"}]
                            [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
                            [:link {:rel "shortcut icon" :href "data:,"}]
                            [:link {:rel "apple-touch-icon" :href "data:,"}]
                            ;; [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css"}]
                            "
    <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">
    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>
    <link href=\"https://fonts.googleapis.com/css2?family=Roboto&display=swap\" rel=\"stylesheet\">
"
                            [:style styles/table]
                            [:style styles/loader]
                            [:style
                             (-> #_"highlight/styles/tokyo-night-light.min.css"
                                 #_"highlight/styles/stackoverflow-light.min.css"
                                 "highlight/styles/qtcreator-light.min.css"
                                 #_"highlight/styles/nord.min.css"
                                 io/resource
                                 slurp)]
                            (css-from-local-copies "https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/css/bootstrap.min.css")
                            [:style "
code {
  font-family: Fira Code,monospace,Consolas,courier new;
  color: black;
  background-color: #f0f0f0;
  padding: 2px;
  .bg-light;
}"]

                            (when toc?
                              (css-from-local-copies
                               #_"https://cdn.jsdelivr.net/npm/bootswatch@4.5.2/dist/sandstone/bootstrap.min.css"
                               #_"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
                               "https://cdn.rawgit.com/afeld/bootstrap-toc/v1.0.1/dist/bootstrap-toc.min.css"))
                            (when toc?
                              [:style styles/boostrap-toc])
                            (->> special-libs
                                 (mapcat (comp :from-local-copy :css special-lib-resources))
                                 distinct
                                 (apply css-from-local-copies))
                            (->> special-libs
                                 (mapcat (comp :from-the-web :css special-lib-resources))
                                 distinct
                                 (apply hiccup.page/include-css))
                            [:title (or title "Clay")]]
                           [:body  {:style {:background "#fcfcfc"
                                            :font-family "'Roboto', sans-serif"
                                            :width "90%"
                                            :margin "auto"}
                                    :data-spy "scroll"
                                    :data-target "#toc"}

                            (js-from-local-copies "https://code.jquery.com/jquery-3.6.0.min.js"
                                                  "https://code.jquery.com/ui/1.13.1/jquery-ui.min.js"
                                                  "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js")
                            (when toc?
                              (js-from-local-copies
                               "https://cdn.rawgit.com/afeld/bootstrap-toc/v1.0.1/dist/bootstrap-toc.min.js"))
                            (js-from-local-copies
                             "https://unpkg.com/react@18/umd/react.production.min.js"
                             "https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"
                             "https://scicloj.github.io/scittle/js/scittle.js"
                             "https://scicloj.github.io/scittle/js/scittle.cljs-ajax.js"
                             "https://scicloj.github.io/scittle/js/scittle.reagent.js"
                             "https://scicloj.github.io/scittle/js/scittle.mathbox.js"
                             "https://scicloj.github.io/scittle/js/scittle.emmy.js"
                             "https://scicloj.github.io/scittle/js/scittle.tmdjs.js")
                            [:script {:type "text/javascript"}
                             (-> "highlight/highlight.min.js"
                                 io/resource
                                 slurp)]
                            (hiccup.page/include-js portal/url)
                            (->> special-libs
                                 (mapcat (comp :from-local-copy :js special-lib-resources))
                                 distinct
                                 (apply js-from-local-copies))
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
                                [:div
                                 (->> widgets
                                      (map-indexed
                                       (fn [i widget]
                                         [:div {:style {:margin "15px"}}
                                          (cond
                                            (-> widget meta :clay/hide-code?)
                                            nil
                                            ;;
                                            (widget/check widget :clay/plain-html?)
                                            widget
                                            ;; widget
                                            ;;
                                            :else
                                            [:div {:id (str "widget" i)}
                                             [:code "loading ..."]])]))
                                      (into [:div]))]]]]]
                            [:script {:type "text/javascript"}
                             "hljs.highlightAll();"]
                            [:script {:type "application/x-scittle"}
                             (->> {:widgets widgets
                                   :data data
                                   :port port
                                   :special-libs special-libs
                                   :server-counter counter}
                                  cljs-generation/widgets-cljs
                                  (map pr-str)
                                  (string/join "\n"))]])
        (string/replace #"<table>"
                        "<table class='table table-hover'>"))))


(defn qmd [{:keys [data port title options counter]}
           widgets]
  (let [special-libs (->> widgets
                          special-libs-in-form)]
    (when-not port
      (throw (ex-info "missing port" {})))
    (str
     (->> options
          :quarto
          yaml/generate-string
          (format "\n---\n%s\n---\n"))

     "
#
"
     (-> (str (hiccup/html
               [:div
                [:style styles/table]
                [:style styles/loader]
                [:style "
.printedClojure .sourceCode {
  background-color: transparent;
  border-style: none;
}
"]
                (hiccup.page/include-js portal/url)
                (->> special-libs
                     (mapcat (comp :from-local-copy :css special-lib-resources))
                     distinct
                     (apply css-from-local-copies))
                (->> special-libs
                     (mapcat (comp :from-the-web :css special-lib-resources))
                     distinct
                     (apply hiccup.page/include-css))])
              (hiccup/html
               [:div
                (js-from-local-copies "https://code.jquery.com/jquery-3.6.0.min.js"
                                      "https://code.jquery.com/ui/1.13.1/jquery-ui.min.js"
                                      #_"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js")
                (js-from-local-copies
                 "https://unpkg.com/react@17/umd/react.production.min.js"
                 "https://unpkg.com/react-dom@17/umd/react-dom.production.min.js"
                 "https://scicloj.github.io/scittle/js/scittle.js"
                 "https://scicloj.github.io/scittle/js/scittle.cljs-ajax.js"
                 "https://scicloj.github.io/scittle/js/scittle.reagent.js"
                 "https://scicloj.github.io/scittle/js/scittle.mathbox.js"
                 "https://scicloj.github.io/scittle/js/scittle.emmy.js"
                 "https://scicloj.github.io/scittle/js/scittle.tmdjs.js")
                (->> special-libs
                     (mapcat (comp :from-local-copy :js special-lib-resources))
                     distinct
                     (apply js-from-local-copies))
                (->> special-libs
                     (mapcat (comp :from-the-web :js special-lib-resources))
                     distinct
                     (apply hiccup.page/include-js))
                (->> widgets
                     (map-indexed
                      (fn [i widget]
                        (cond
                          ;;
                          (-> widget
                              meta
                              :clay/original-markdown)
                          (-> widget
                              meta
                              :clay/original-markdown)
                          ;;
                          (-> widget
                              meta
                              :clay/original-code)
                          (let [code (if (-> widget
                                             meta
                                             :clay/hide-code?)
                                       ""
                                       (-> widget
                                           meta
                                           :clay/original-code))]
                            (if (= code "")
                              "
```
```
"
                              (->> code
                                   (format "
<div class=\"originalCode\">
```clojure
%s
```
</div>

"))))
                          ;;
                          ;;
                          (widget/check widget :clay/printed-clojure?)
                          (->> widget
                               meta
                               :clay/text
                               (format "
<div class=\"printedClojure\">
```clojure
%s
```
</div>
"))
                          ;;
                          (widget/check widget :clay/plain-html?)
                          (hiccup/html widget)
                          ;;
                          :else
                          (hiccup/html
                           [:div {:id (str "widget" i)}
                            [:code "loading ..."]]))))
                     (string/join "\n"))
                [:script {:type "application/x-scittle"}
                 (->> {:widgets widgets
                       :data data
                       :port port
                       :special-libs special-libs
                       :server-counter counter}
                      cljs-generation/widgets-cljs
                      (map pr-str)
                      (string/join "\n"))]]))
         (string/replace #"<table>"
                         "<table class='table table-hover'>")))))

(defn signals-of-no-plain-html? [hiccup]
  (or (->> hiccup
           special-libs-in-form
           (some (partial (complement #{'datatables 'vega}))))
      (->> hiccup
           flatten
           (some #{'fn 'quote}))))

(defn light-qmd [{:keys [data title options counter]}
                 widgets]
  (let [special-libs (->> widgets
                          special-libs-in-form)]
    (str
     (->> options
          :quarto
          yaml/generate-string
          (format "\n---\n%s\n---\n"))

     ;; " "
     (hiccup/html
      [:style styles/table]
      [:style "
.printedClojure .sourceCode {
  background-color: transparent;
  border-style: none;
}
"])
     (when (special-libs 'vega)
       (->> 'vega
            special-lib-resources
            :js
            :from-local-copy
            (apply hiccup.page/include-js)
            hiccup/html))
     (when (special-libs 'datatables)
       (str (->> 'datatables
                 special-lib-resources
                 :js
                 :from-the-web
                 (cons "https://code.jquery.com/jquery-3.6.0.min.js")
                 (apply hiccup.page/include-js)
                 hiccup/html)
            (->> 'datatables
                 special-lib-resources
                 :css
                 :from-the-web
                 (apply hiccup.page/include-css)
                 hiccup/html)))
     (->> widgets
          (map-indexed
           (fn [i widget]
             (cond
               ;;
               (-> widget
                   meta
                   :clay/original-markdown)
               (-> widget
                   meta
                   :clay/original-markdown)
               ;;
               (-> widget
                   meta
                   :clay/original-code)
               (let [code (if (-> widget
                                  meta
                                  :clay/hide-code?)
                            ""
                            (-> widget
                                meta
                                :clay/original-code))]
                 (if (= code "")
                   "
```
```
"
                   (->> code
                        (format "
<div class=\"originalCode\">
```clojure
%s
```
</div>

"))))
               ;;
               ;;
               (widget/check widget :clay/printed-clojure?)
               (->> widget
                    meta
                    :clay/text
                    (format "
<div class=\"printedClojure\">
```clojure
%s
```
</div>
"))
               ;;
               (widget/check widget :clay/plain-html?)
               (hiccup/html widget)
               ;;
               (not (signals-of-no-plain-html? widget))
               (try
                 (hiccup/html
                  widget)
                 (catch Exception e "
**unsupported element**
"))
               ;;
               :else
               "
**unsupported element**
")))
          (string/join "\n")))))
