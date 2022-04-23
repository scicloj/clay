(ns scicloj.clay.v1.tool.scittle.page
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [hiccup.page]
            [scicloj.clay.v1.tool.scittle.cljs-generation :as cljs-generation]
            [scicloj.clay.v1.tool.scittle.widget :as widget]
            [scicloj.clay.v1.html.table :as table]))



(defn page [{:keys [widgets data reveal? port title]}]
  (when-not port
    (throw (ex-info "missing port")))
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
                          [:style table/style]
                          [:style
                           (-> #_"highlight/styles/tokyo-night-light.min.css"
                               #_"highlight/styles/stackoverflow-light.min.css"
                               "highlight/styles/qtcreator-light.min.css"
                               #_"highlight/styles/nord.min.css"
                               io/resource
                               slurp)]

                          [:script {:type "text/javascript"}
                           (-> "highlight/highlight.min.js"
                               io/resource
                               slurp)]
                          [:script {:crossorigin nil :src "https://unpkg.com/react@17/umd/react.production.min.js"}]
                          [:script {:crossorigin nil :src "https://unpkg.com/react-dom@17/umd/react-dom.production.min.js"}]
                          [:script {:src "https://code.jquery.com/jquery-3.6.0.min.js"
                                    :integrity "sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4="
                                    :crossorigin "anonymous"}]
                          [:script {:src "https://code.jquery.com/ui/1.13.1/jquery-ui.min.js"
                                    :integrity "sha256-eTyxS0rkjpLEo16uXTS0uVCS4815lc40K2iVpWDvdSY="
                                    :crossorigin "anonymous"}]
                          (hiccup.page/include-js
                           #_"https://cdn.tailwindcss.com"
                           "https://cdn.jsdelivr.net/npm/scittle@0.1.2/dist/scittle.js"
                           "https://cdn.jsdelivr.net/npm/scittle@0.1.2/dist/scittle.cljs-ajax.js"
                           "https://cdn.jsdelivr.net/npm/scittle@0.1.2/dist/scittle.reagent.js"
                           "https://cdn.jsdelivr.net/npm/echarts@5.3.2/dist/echarts.min.js"
                           "https://cdnjs.cloudflare.com/ajax/libs/cytoscape/3.21.1/cytoscape.min.js"
                           "https://cdn.jsdelivr.net/npm/vega@5.22.1"
                           "https://cdn.jsdelivr.net/npm/vega-lite@5.2.0"
                           "https://cdn.jsdelivr.net/npm/vega-embed@6.20.8")
                          (hiccup.page/include-js
                           "https://cdn.datatables.net/1.11.5/js/jquery.dataTables.min.js")
                          (hiccup.page/include-css
                           "https://cdn.datatables.net/1.11.5/css/jquery.dataTables.min.css")
                          (when reveal?
                            (hiccup.page/include-css
                             "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/3.6.0/css/reveal.min.css")
                            (hiccup.page/include-js
                             "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/3.6.0/js/reveal.min.js"))
                          [:link {:rel "stylesheet"
                                  :href "https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/css/bootstrap.min.css"
                                  :integrity "sha384-zCbKRCUGaJDkqS1kPbPd7TveP5iyJE0EjAuZQTgFLD2ylzuqKfdKlfG/eSrtxUkn"
                                  :crossorigin "anonymous"}]
                          (hiccup.page/include-css
                           #_"https://cdn.jsdelivr.net/npm/bootswatch@4.5.2/dist/sandstone/bootstrap.min.css"
                           #_"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
                           "https://cdn.rawgit.com/afeld/bootstrap-toc/v1.0.1/dist/bootstrap-toc.min.css")
                          [:style "
code {
  font-family: Fira Code,Consolas,courier new;
  color: black;
  background-color: #f0f0f0;
  padding: 2px;
  .bg-light;
}"]
                          ;; https://afeld.github.io/bootstrap-toc/#customization
                          [:style "
nav[data-toggle=\"toc\"] {
  top: 42px;
}

nav[data-toggle=toc] .nav-link+ul {
 display:block;
 padding-bottom:10px;
}


/* small screens */
@media (max-width: 768px) {
  /* override stickyness so that the navigation does not follow scrolling */
  nav[data-toggle=\"toc\"] {
    margin-bottom: 42px;
    position: static;
  }

  /* PICK ONE */
  /* don't expand nested items, which pushes down the rest of the page when navigating */
  nav[data-toggle=\"toc\"] .nav .active .nav {
    display: none;
  }
  /* alternatively, if you *do* want the second-level navigation to be shown (as seen on this page on mobile), use this */
/*
nav[data-toggle='toc'] .nav .nav {
    display: block;
  }
*/
}
"]
                          [:title (or title "Clay")]]
                         [:body  {:style {
                                          ;;:background "#f6f6f6"
                                          ;;:font-family "'Roboto', sans-serif"
                                          ;; :width "90%"
                                          ;; :margin "auto"
                                          }
                                  :data-spy "scroll"
                                  :data-target "#toc"}
                          [:div.container
                           [:div.row
                            [:div.col-sm-3
                             [:nav.sticky-top {:id "toc"
                                               :data-toggle "toc"}]]
                            [:div.col-sm-9
                             [:div
                              #_(when reveal? {:class :reveal})
                              [:div #_(when reveal? {:class :slides})
                               (->> widgets
                                    (map-indexed
                                     (fn [i widget]
                                       (if (widget/plain-html? widget)
                                         widget
                                         (cond->> [:div {:id (str "widget" i)}]
                                           reveal? (vector :section)))))
                                    (into [:div]))]]]]
                           #_(when reveal?
                               [:script {:type "application/x-scittle"}
                                (pr-str '(.initialize js/Reveal (clj->js {:hash true})))])]
                          [:script
                           {:src "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
                            :integrity "sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
                            :crossorigin "anonymous"}]
                          (hiccup.page/include-js
                           "https://cdn.rawgit.com/afeld/bootstrap-toc/v1.0.1/dist/bootstrap-toc.min.js")
                          [:script {:type "text/javascript"}
                           "hljs.highlightAll();"]
                          ;; [:script {:type "text/javascript"}
                          ;;                            "
                          ;;     //Executes your code when the DOM is ready.  Acts the same as $(document).ready().
                          ;;               $(function() {
                          ;;                   //Calls the tocify method on your HTML div.
                          ;;                   $(\"#toc\").tocify();
                          ;;               });
                          ;; "]
                          [:script {:type "application/x-scittle"}
                           (->> (cljs-generation/widgets-cljs widgets data port)
                                (map pr-str)
                                (string/join "\n"))]])
      (string/replace #"<table>"
                      "<table class='table table-hover'>")))
