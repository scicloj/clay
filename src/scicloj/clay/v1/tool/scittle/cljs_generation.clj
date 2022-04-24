(ns scicloj.clay.v1.tool.scittle.cljs-generation
  (:require [scicloj.clay.v1.tool.scittle.widget :as widget]))

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
     ([echart-option {:keys [on register-map]
                      :as options}]
      (fn []
        [:div
         {:style {:height "500px"}
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


(def special-libs-cljs
  {'datatables datatables-cljs
   'vega vega-cljs
   'echarts echarts-cljs
   'cytoscape cytoscape-cljs})


(defn widgets-cljs [{:keys [widgets data port special-libs]}]
  (concat ['(ns main
              (:require [reagent.core :as r]
                        [reagent.dom :as dom]
                        [ajax.core :refer [GET POST]]
                        [clojure.string :as string]))
           (list 'def 'data
                 (list 'quote data))
           `(let [socket (js/WebSocket. (str "ws://localhost:" ~port))]
              (.addEventListener socket "open" (fn [event]
                                                 (.send socket "Hello Server!")))
              (.addEventListener socket "message" (fn [event]
                                                    (case (.-data event)
                                                      "refresh" (.reload js/location)
                                                      (println [:unknown-ws-message (.-data event)])))))

           '(defn compute [[fname & args] result-state result-path]
              (POST "/compute"
                    {:headers {"Accept" #_"application/transit+json" "application/json"}
                     :params (pr-str {:fname fname
                                      :args args})
                     :handler (fn [response]
                                (swap! result-state
                                       assoc-in
                                       result-path (read-string response)))
                     :error-handler (fn [e]
                                      (.log js/console (str e)))}))]
          (->> special-libs
               (map special-libs-cljs))
          (->> widgets
               (map-indexed
                (fn [i widget]
                  (when-not (widget/plain-html? widget)
                    (let [widget-name (str "widget" i)
                          widget-symbol (symbol (str "widget" i))]
                      [(list 'def widget-symbol widget)
                       (list 'dom/render
                             (list 'fn []
                                   widget-symbol)
                             (list '.getElementById 'js/document widget-name))]))))
               (apply concat))))
