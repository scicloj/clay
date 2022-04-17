(ns scicloj.clay.v1.tool.scittle.page
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [hiccup.page]
            [scicloj.clay.v1.tool.scittle.cljs-generation :as cljs-generation]
            [scicloj.clay.v1.tool.scittle.widget :as widget]))

(defn page [{:keys [widgets data reveal? port]}]
  (when-not port
    (throw (ex-info "missing port")))
  (hiccup.page/html5
   [:head
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
    [:style
     (-> #_"highlight/styles/tokyo-night-light.min.css"
         "highlight/styles/stackoverflow-light.min.css"
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
    (hiccup.page/include-js
     "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.1/js/scittle.js"
     "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.1/js/scittle.reagent.js"
     "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.1/js/scittle.cljs-ajax.js"
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
    (hiccup.page/include-css
     "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css")
    [:title "Clay"]]
   [:body  {:style {:background "#f1f1f1"
                    :font-family "'Roboto', sans-serif"
                    :width "90%"
                    :margin "auto"}}
    [:div (when reveal? {:class :reveal})
     [:div (when reveal? {:class :slides})
      (->> widgets
           (map-indexed
            (fn [i widget]
              (if (widget/plain-html? widget)
                widget
                (cond->> [:div {:id (str "widget" i)}]
                  reveal? (vector :section)))))
           (into [:div]))
      [:script {:type "application/x-scittle"}
       (->> (cljs-generation/widgets-cljs widgets data port)
            (map pr-str)
            (string/join "\n"))]]]
    (when reveal?
      [:script {:type "application/x-scittle"}
       (pr-str '(.initialize js/Reveal (clj->js {:hash true})))])
    [:script {:type "text/javascript"}
     "hljs.highlightAll();"]]))
