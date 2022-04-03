(ns scicloj.clay.v1.html.vega
  (:require [hiccup.core :as hiccup]
            [hiccup.element :as elem]
            [cheshire.core :as cheshire]
            [clojure.walk :refer [postwalk]])
  (:import java.util.UUID))


(defn vegas->html
  "https://vega.github.io/vega-lite/tutorials/getting_started.html#embed"
  [{:keys [cdn?]} & v-specs]
  (->> v-specs
       (map (fn [v-spec]
              (let [div-id (str "vis" (UUID/randomUUID))]
                (str
                 (hiccup/html
                  [:div {:id div-id}]
                  (elem/javascript-tag
                   (str "vg.embed(\"#" div-id "\", "
                        (cheshire/generate-string v-spec) ", "
                        "  function(error, result) {});")))))))
       (apply str (if cdn? (cdn/scripts :vega-lite))
              "<style media=\"screen\">"
              "    /* Add space between vega-embed links */ "
              "  .vega-actions a { "
              "    margin-right: 5px;"
              "  }"
              " </style>")))
