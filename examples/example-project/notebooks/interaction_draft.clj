(ns interaction-draft
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.clay.v2.tool.scittle :as tool.scittle]
            [scicloj.clay.v2.util.image :as util.image]
            [scicloj.viz.api :as viz]
            [tablecloth.api :as tc]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as fun]
            [tech.v3.tensor :as tensor]))

;; ## Widgets with Scittle

(let [data {:day ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]
            :rain [23 24 18 25 27 28 25]}]
  (tool.scittle/show-widgets!
   ['[(fn []
        (let [*state (r/atom {})]
          (fn []
            [:div
             [:p "click the bars to ask the server for each day's information"]
             [echarts
              {:xAxis {:data (:day data)}
               :yAxis {}
               :series [{:type "bar"
                         :data (:rain data)}]}
              {:on {:click #(do
                              (println (-> %  js->clj pr-str))
                              (compute [:fn1
                                        (-> % .-dataIndex)]
                                       *state [:day]))}}]
             [:hr]
             [:h1 (:day @*state)]])))]]
   {:data data
    :fns {:fn1 (fn [i]
                 (-> data
                     :day
                     (get i)))}}))
