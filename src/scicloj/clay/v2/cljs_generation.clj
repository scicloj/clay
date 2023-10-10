(ns scicloj.clay.v2.cljs-generation
  (:require
   [scicloj.clay.v2.item :as item]))

(def datatables-cljs
  '(defn datatables [table]
     (let [[_ options thead tbody] table]
       [:div
        [:table
         (merge options
                {:ref (fn [el]
                        (-> el
                            js/$
                            (.DataTable (clj->js {:sPaginationType "full_numbers"
                                                  :order []}))))})
         thead
         tbody]])))

(def vega-cljs
  '(defn vega [spec]
     [:div
      {:ref (fn [el]
              (-> el
                  (js/vegaEmbed (clj->js spec)
                                (clj->js {:renderer :svg}))
                  (.then (fn [res]))
                  (.catch (fn [err] (println (str "vegaEmbed error: " err))))))}]))


(def echarts-cljs
  '(defn echarts
     ([echart-option]
      (echarts echart-option nil))
     ([echart-option {:keys [on register-map div-style]
                      :or {:div-style {:height 500}}
                      :as options}]
      (fn []
        [:div
         {:style div-style
          :ref (fn [el]
                 (when el
                   (when-let [[map-name map-options] register-map]
                     (.registerMap js/echarts
                                   map-name (clj->js map-options)))
                   (let [chart (.init js/echarts el)]
                     (->> echart-option
                          clj->js
                          (.setOption chart))
                     (some->> on
                              (run! (fn [[event-type f]]
                                      (.on chart
                                           (name event-type)
                                           f)))))))}]))))

(def cytoscape-cljs
  '(defn cytoscape
     ([value]
      (cytoscape value {:style {:height "500px"}}))
     ([value {:keys [style]}]
      [:div
       {:style style
        :ref (fn [el]
               (when el
                 (-> value
                     (assoc :container el)
                     clj->js
                     js/cytoscape)))}])))


(def plotly-cljs
  '(defn plotly
     [{:keys [data]}]
     [:div
      {:ref (fn [el]
              (when el
                (.newPlot js/Plotly
                          el
                          (clj->js data))))}]))

(def katex-cljs
  '(defn katex
     [tex-string]
     [:div
      {:dangerouslySetInnerHTML
       {:__html (.renderToString js/katex
                                 tex-string)}}]))


(def three-d-mol-viewer-cljs
  '(defn three-d-mol-viewer
     [{:keys [data-pdb]}]
     [:div {:style {:height "400px"
                    :width "400px"
                    :position :relative}
            :class "viewer_3Dmoljs"
            :data-pdb data-pdb
            :data-backgroundcolor "0xffffff"
            :data-style "stick"
            :data-ui true}]))

(def special-libs-cljs
  {'datatables datatables-cljs
   'vega vega-cljs
   'echarts echarts-cljs
   'cytoscape cytoscape-cljs
   'plotly plotly-cljs
   'katex katex-cljs
   'three-d-mol-viewer three-d-mol-viewer-cljs})

(defn items-cljs [{:keys [server-counter items data port special-libs]}]
  (concat ['(ns scicloj.clay
              (:require [reagent.core :as r]
                        [ajax.core :refer [GET POST]]))
           '(def *cache
              (r/atom {:map {}
                       :vec []}))
           '(defn reset-cache! [kv-pairs]
              (reset! *cache
                      {:map (if (map? kv-pairs)
                              kv-pairs
                              (into {} kv-pairs))
                       :vec kv-pairs}))
           '(defn compute [form]
              (if-let [result (-> @*cache
                                  :map
                                  (get form))]
                result
                (do (POST "/compute"
                          {:headers {"Accept" "application/json"}
                           :params (pr-str {:form form})
                           :handler (fn [response-edn]
                                      (let [response (read-string response-edn)]
                                        (swap! *cache
                                               (fn [cache]
                                                 (-> cache
                                                     (update :map assoc form response)
                                                     (update :vec conj [form response]))))))
                           :error-handler (fn [e]
                                            (.log
                                             js/console
                                             (str "error on compute: " e)))})
                    [:div
                     [:p "computing"]
                     [:pre [:code (pr-str form)]]
                     [:div.loader]])))
           '(defn refresh-page []
              (.reload js/location))
           '(ns main
              (:require [reagent.core :as r]
                        [reagent.dom :as dom]
                        [ajax.core :refer [GET POST]]
                        [clojure.string :as string]
                        [scicloj.clay :as clay]))
           (list 'GET "/counter"
                 {:handler (list
                            'fn '[response]
                            (list 'when
                                  (list 'not= 'response
                                        `(str ~server-counter))
                                  '(clay/refresh-page)))
                  :error-handler '(fn [e]
                                    (.log
                                     js/console
                                     (str "error on counter: " e)))})
           (list 'def 'data
                 (list 'quote data))
           (list 'let ['socket `(js/WebSocket. (str "ws://localhost:" ~port))]
                 '(.addEventListener socket "open" (fn [event]
                                                     (.send socket "Hello Server!")))
                 '(.addEventListener socket "message" (fn [event]
                                                        (case (.-data event)
                                                          "refresh" (clay/refresh-page)
                                                          (println [:unknown-ws-message (.-data event)])))))]
          (->> special-libs
               (map special-libs-cljs))
          #_(->> items
                 (map-indexed
                  (fn [i item]
                    (when-let [{:keys [reagent]} item]
                      (let [item-name (str "item" i)
                            item-symbol (symbol (str "item" i))]
                        [(list 'def item-symbol reagent)
                         (list 'dom/render
                               (list 'fn []
                                     item-symbol)
                               (list '.getElementById 'js/document item-name))]))))
                 (apply concat))))
