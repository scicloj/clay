(ns scicloj.clay.v2.tool.scittle.cljs-generation
  (:require [scicloj.clay.v2.tool.scittle.widget :as widget]))

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
                          (clj->js data)
                          )))}]))
(def special-libs-cljs
  {'datatables datatables-cljs
   'vega vega-cljs
   'echarts echarts-cljs
   'cytoscape cytoscape-cljs
   'plotly plotly-cljs})

(defn widgets-cljs [{:keys [server-counter widgets data port special-libs]}]
  (concat ['(ns scicloj.clay
              (:require [reagent.core :as r]
                        [ajax.core :refer [GET POST]]))
           '(def *cache
              (r/atom {}))
           '(defn reset-cache! [m]
              (reset! *cache m))
           '(defn compute [form]
              (if-let [result (@*cache form)]
                result
                (do (POST "/compute"
                          {:headers {"Accept" "application/json"}
                           :params (pr-str {:form form})
                           :handler (fn [response]
                                      (swap! *cache
                                             assoc
                                             form (read-string response))
                                      (.log
                                       js/console
                                       (pr-str @*cache)))
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
          (->> widgets
               (map-indexed
                (fn [i widget]
                  (when-not (widget/check widget :clay/plain-html?)
                    (let [widget-name (str "widget" i)
                          widget-symbol (symbol (str "widget" i))]
                      [(list 'def widget-symbol widget)
                       (list 'dom/render
                             (list 'fn []
                                   widget-symbol)
                             (list '.getElementById 'js/document widget-name))]))))
               (apply concat))))
