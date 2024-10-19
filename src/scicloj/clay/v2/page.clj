(ns scicloj.clay.v2.page
  (:require
   [clj-yaml.core :as yaml]
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   [clojure.string :as string]
   [hiccup.core :as hiccup]
   [hiccup.page]
   [scicloj.clay.v2.prepare :as prepare]
   [scicloj.clay.v2.styles :as styles]
   [scicloj.clay.v2.util.portal :as portal]
   [scicloj.clay.v2.util.resource :as resource]
   [scicloj.clay.v2.files :as files]))

(def special-lib-resources
  {:vega {:js {:from-local-copy
               ["https://cdn.jsdelivr.net/npm/vega@5.25.0"
                "https://cdn.jsdelivr.net/npm/vega-lite@5.16.3"
                "https://cdn.jsdelivr.net/npm/vega-embed@6.22.2"]}}
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
                ["https://cdn.jsdelivr.net/npm/katex@0.16.10/dist/katex.min.js"]}
           :css {:from-the-web
                 ;; fetching the KaTeX css from the web
                 ;; to avoid fetching the fonts locally,
                 ;; which would need a bit more care
                 ;; (see https://katex.org/docs/font.html)
                 ["https://cdn.jsdelivr.net/npm/katex@0.16.10/dist/katex.min.css"]}}
   :three-d-mol {:js {:from-the-web
                      ["https://cdnjs.cloudflare.com/ajax/libs/3Dmol/1.5.3/3Dmol.min.js"]}}
   :leaflet {;; fetching Leaflet from the web
             ;; to avoid fetching the images locally,
             ;; which would need a bit more care.
             :js {:from-the-web
                  ["https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
                   "https://cdn.jsdelivr.net/npm/leaflet-providers@2.0.0/leaflet-providers.min.js"]}
             :css {:from-the-web
                   ["https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"]}}
   :reagent {:js {:from-local-copy
                  ["https://unpkg.com/react@18/umd/react.production.min.js"
                   "https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"
                   "https://daslu.github.io/scittle/js/scittle.js"
                   "https://daslu.github.io/scittle/js/scittle.cljs-ajax.js"
                   "https://daslu.github.io/scittle/js/scittle.reagent.js"
                   "https://cdn.jsdelivr.net/npm/d3-require@1"]}}
   ;; :tmdjs {:js {:from-local-copy
   ;;              ["https://daslu.github.io/scittle/js/scittle.tmdjs.js"]}}
   :emmy {:js {:from-local-copy
               ["https://daslu.github.io/scittle/js/scittle.emmy.js"]}}
   :emmy-viewers {:js {:from-local-copy
                       ["https://daslu.github.io/scittle/js/scittle.emmy.js"
                        "https://daslu.github.io/scittle/js/scittle.emmy-viewers.js"]}
                  :css {:from-local-copy
                        ["https://unpkg.com/mafs@0.18.8/core.css"
                         "https://unpkg.com/mafs@0.18.8/font.css"
                         "https://unpkg.com/mathbox@2.3.1/build/mathbox.css"
                         "https://unpkg.com/mathlive@0.85.1/dist/mathlive-static.css"
                         "https://unpkg.com/mathlive@0.85.1/dist/mathlive-fonts.css"]}}
   ;; :mathbox {:js {:from-local-copy
   ;;                ["https://daslu.github.io/scittle/js/scittle.mathbox.js"]}}
   :portal {:js {:from-local-copy [portal/url]}}
   :d3 {:js {:from-local-copy
             ["https://cdn.jsdelivr.net/npm/d3@7"]}}
   :html-default {:js {:from-local-copy
                       ["https://code.jquery.com/jquery-3.6.0.min.js"
                        "https://code.jquery.com/ui/1.13.1/jquery-ui.min.js"]}}
   :md-default {:js {:from-local-copy
                     ["https://code.jquery.com/jquery-3.6.0.min.js"
                      "https://code.jquery.com/ui/1.13.1/jquery-ui.min.js"]}}
   :htmlwidgets-ggplotly {:js {:from-local-copy-of-repo
                               [{:gh-repo "scicloj/ggplotly-deps"
                                 :relative-path "lib"
                                 :paths ["htmlwidgets-1.6.2/htmlwidgets.js"
                                         "plotly-binding-4.10.4.9000/plotly.js"
                                         "typedarray-0.1/typedarray.min.js"
                                         "jquery-3.5.1/jquery.min.js"
                                         "crosstalk-1.2.1/js/crosstalk.min.js"
                                         "plotly-main-2.11.1/plotly-latest.min.js"]}]}
                          :css {:from-local-copy-of-repo
                                [{:gh-repo "scicloj/ggplotly-deps"
                                  :relative-path "lib"
                                  :paths ["crosstalk-1.2.1/css/crosstalk.min.css"
                                          "plotly-htmlwidgets-css-2.11.1/plotly-htmlwidgets.css"]}]}}
   :highcharts {:js {:from-the-web ["https://code.highcharts.com/highcharts.js"]}}})

(def include
  {:js hiccup.page/include-js
   :css hiccup.page/include-css})

(defn include-inline [js-or-css]
  (fn [url]
    (->> url
         ((include js-or-css))
         (map (fn [script-tag]
                (let [{:keys [src]} (second script-tag)]
                  (-> script-tag
                      (conj (slurp src))
                      (update 1 dissoc :src))))))))

(defn include-from-a-local-file [url custom-name js-or-css
                                 {:keys [full-target-path base-target-path]}]
  (let [path (files/next-file!
              full-target-path
              custom-name
              url
              (str "." (name js-or-css)))]
    (io/make-parents path)
    (->> url
         resource/get
         (spit path))
    (-> path
        (string/replace
         (re-pattern (str "^"
                          base-target-path
                          "/"))
         "")
        ((include js-or-css)))))

(defn clone-repo-if-needed! [gh-repo]
  (let [target-path (str "/tmp/.clay/clones/" gh-repo)]
    (io/make-parents target-path)
    (when-not
        (.exists (io/file target-path))
      (let [repo-url (str "https://github.com/" gh-repo)]
        (prn [:cloning repo-url])
        (shell/sh "git" "clone" repo-url target-path)))
    target-path))

(defn include-from-a-local-copy-of-repo [{:as details
                                          :keys [gh-repo relative-path paths]}
                                         lib
                                         js-or-css
                                         {:keys [base-target-path]}]
  (let [repo-path (clone-repo-if-needed! gh-repo)
        target-repo-path (str (name lib) "/gh-repos/" gh-repo)
        target-copy-path (str base-target-path "/" target-repo-path)]
    (when-not (.exists (io/file target-copy-path))
      (babashka.fs/copy-tree (str repo-path "/" relative-path)
                             target-copy-path))
    (->> paths
         (map (fn [path]
                (->> path
                     (str target-repo-path "/")
                     ((include js-or-css))))))))

(defn include-libs-hiccup [{:as spec :keys [inline-js-and-css]}
                           deps-types libs]
  (->> deps-types
       (mapcat (fn [js-or-css]
                 (->> libs
                      (mapcat
                       (fn [lib]
                         (->> lib
                              special-lib-resources
                              js-or-css
                              ((fn [{:keys [from-the-web
                                            from-local-copy
                                            from-local-copy-of-repo]}]
                                 (if inline-js-and-css
                                   (->> (concat from-the-web
                                                from-local-copy
                                                from-local-copy-of-repo)
                                        (map (include-inline js-or-css)))
                                   ;; else
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
                                                     spec))))
                                    (some->> from-local-copy-of-repo
                                             (map (fn [details]
                                                    (include-from-a-local-copy-of-repo
                                                     details
                                                     lib
                                                     js-or-css
                                                     spec))))))))))))))
       (apply concat)))

(defn include-libs [spec deps-types libs]
  (->> libs
       (include-libs-hiccup spec deps-types)
       hiccup/html
       (format "\n%s\n")))

(def font-links
  " <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">
    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>
    <link href=\"https://fonts.googleapis.com/css2?family=Roboto&display=swap\" rel=\"stylesheet\">
")

(defn items->deps [items]
  (->> items
       (mapcat :deps)
       distinct))

(defn html [{:as spec
             :keys [items title toc? favicon]}]
  (let [special-libs (->> items
                          items->deps
                          (concat [:html-default :katex]))
        head [:head
              [:meta {:charset "UTF-8"}]
              [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
              (when favicon
                [:link {:rel "icon" :href favicon}])
              #_[:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css"}]
              font-links
              [:style (styles/main :table)]
              [:style (styles/main :loader)]
              [:style (styles/main :code)]
              [:style (styles/main :bootswatch-cosmo-bootstrap.min)]
              [:style (styles/main :bootstrap-generated-by-quarto.min)]
              [:style (styles/highlight :panda-syntax-light)]
              [:style (styles/main :main)]
              (when toc?
                (include-from-a-local-file
                 "https://cdn.rawgit.com/afeld/bootstrap-toc/v1.0.1/dist/bootstrap-toc.min.css"
                 "bootstrap-toc"
                 :css
                 spec))
              (when toc?
                [:style (styles/main :bootstrap-toc-customization)])
              (include-libs spec [:css] special-libs)
              [:title (or title "Clay")]]
        body [:body  {:style {;;:background "#fcfcfc"
                              ;; :font-family "'Roboto', sans-serif"
                              ;; :width "95%"
                              :margin "auto"}
                      :data-spy "scroll"
                      :data-target "#toc"}
              (when toc?
                (include-from-a-local-file
                 "https://cdn.rawgit.com/afeld/bootstrap-toc/v1.0.1/dist/bootstrap-toc.min.js"
                 "bootstrap-toc"
                 :js
                 spec))
              (include-libs spec [:js] special-libs)
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
                           (prepare/item->hiccup item spec)]))
                       (into [:div]))]]]]
              [:script {:type "text/javascript"}
               (-> "highlight/highlight.min.js"
                   io/resource
                   slurp)]
              [:script {:type "text/javascript"}
               "hljs.highlightAll();"]]]
    (hiccup.page/html5 head body)))

(defn md [{:as spec
           :keys [items title favicon quarto format]}]
  (let [quarto-target (if (=  format [:quarto :revealjs])
                        :revealjs
                        :html)]
    (str
     "\n---\n"
     (yaml/generate-string
      (cond-> quarto
        ;; Users may provide non-quarto specific configuration (see also html),
        ;; if so this will be added to the quarto front-matter to make them behave the same way
        title (assoc-in [:format :html :title] title)
        favicon (update-in [:format quarto-target :include-in-header :text]
                           str "<link rel = \"icon\" href = \"" favicon "\" />")))
     "\n---\n"
     (hiccup/html
         [:style (styles/main :table)]
       [:style (styles/main :md-main)]
       [:style (styles/main :main)])
     (->> items
          items->deps
          (cons :md-default)
          (include-libs spec [:js :css]))
     (->> items
          (map-indexed
           (fn [i item]
             (prepare/item->md item)))
          (string/join "\n\n")))))


(defn hiccup [{:as spec
               :keys [items title quarto]}]
  (vec (concat (->> items
                    items->deps
                    (include-libs-hiccup spec [:js :css]))
               (->> items
                    (map #(prepare/item->hiccup % spec))))))
