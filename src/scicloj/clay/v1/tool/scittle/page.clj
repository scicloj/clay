(ns scicloj.clay.v1.tool.scittle.page
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [hiccup.page]
            [scicloj.clay.v1.tool.scittle.cljs-generation :as cljs-generation]))

(defn page [{:keys [widgets data reveal? port]}]
  (when-not port
    (throw (ex-info "missing port")))
  (hiccup.page/html5
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:link {:rel "shortcut icon" :href "data:,"}]
     [:link {:rel "apple-touch-icon" :href "data:,"}]
     ;; [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css"}]

     [:script {:crossorigin nil :src "https://unpkg.com/react@17/umd/react.production.min.js"}]
     [:script {:crossorigin nil :src "https://unpkg.com/react-dom@17/umd/react-dom.production.min.js"}]
     [:script {:src "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.1/js/scittle.js" :type "application/javascript"}]
     [:script {:src "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.1/js/scittle.reagent.js" :type "application/javascript"}]
     [:script {:src "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.1/js/scittle.cljs-ajax.js" :type "application/javascript"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/echarts@5.3.2/dist/echarts.min.js"}]
     [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/cytoscape/3.21.1/cytoscape.min.js"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/vega@5.22.1"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/vega-lite@5.2.0"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/vega-embed@6.20.8"}]
     [:script {:src "https://code.jquery.com/jquery-3.6.0.min.js"
               :integrity "sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4="
               :crossorigin "anonymous"}]
     [:script {:src "https://cdn.datatables.net/1.11.5/js/jquery.dataTables.min.js"}]
     [:link {:rel "stylesheet"
             :href "https://cdn.datatables.net/1.11.5/css/jquery.dataTables.min.css"}]
     [:script {:type "text/javascript"}
      (-> "highlight/highlight.min.js"
          io/resource
          slurp)]
     [:style
      (-> "highlight/styles/tokyo-night-light.min.css"
          io/resource
          slurp)]
     [:link {:rel "stylesheet" :href ""}]
     [:link {:rel "stylesheet"
             :href "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/3.6.0/css/reveal.min.css"}]
     [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/3.6.0/js/reveal.min.js"}]
     [:link {:rel "stylesheet"
             :href "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"}]
     [:title "Clay"]]
    [:body
     [:div (when reveal? {:class :reveal})
      [:div (when reveal? {:class :slides})
       (->> widgets
            count
            range
            (map (fn [i]
                   (cond->> [:div {:id (str "widget" i)}]
                     reveal? (vector :section))))
            (into [:div]))
       [:script {:type "application/x-scittle"}
        (->> (cljs-generation/widgets-cljs widgets data port)
             (map pr-str)
             (string/join "\n"))]]]
     (when reveal?
       [:script {:type "application/x-scittle"}
        (pr-str '(.initialize js/Reveal (clj->js {:hash true})))])
     [:script {:type "text/javascript"}
      "hljs.highlightAll();"]]]))
