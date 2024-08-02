(ns currently-unimplemented)

;; ### [Cytoscape.js](https://js.cytoscape.org/)

(def cytoscape-example
  {:elements {:nodes [{:data {:id "a" :parent "b"} :position {:x 215 :y 85}}
                      {:data {:id "b"}}
                      {:data {:id "c" :parent "b"} :position {:x 300 :y 85}}
                      {:data {:id "d"} :position {:x 215 :y 175}}
                      {:data {:id "e"}}
                      {:data {:id "f" :parent "e"} :position {:x 300 :y 175}}]
              :edges [{:data {:id "ad" :source "a" :target "d"}}
                      {:data {:id "eb" :source "e" :target "b"}}]}
   :style [{:selector "node"
            :css {:content "data(id)"
                  :text-valign "center"
                  :text-halign "center"}}
           {:selector "parent"
            :css {:text-valign "top"
                  :text-halign "center"}}
           {:selector "edge"
            :css {:curve-style "bezier"
                  :target-arrow-shape "triangle"}}]
   :layout {:name "preset"
            :padding 5}})

(kind/cytoscape cytoscape-example)

(kind/cytoscape [cytoscape-example
                 {:style {:height 100
                          :width 100}}])

;; ### [Apache Echarts](https://echarts.apache.org/)

(kind/echarts
 {:xAxis {:data ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]}
  :yAxis {}
  :series [{:type "bar"
            :color ["#7F5F3F"]
            :data [23 24 18 25 27 28 25]}]})

;; ### MathBox.cljs

;; inspired by [the official MathBox.cljs tutorial](https://mathbox.mentat.org/)
(kind/hiccup
 '[(let [Data (fn []
                [mathbox.primitives/Interval
                 {:expr (fn [emit x _i t]
                          (emit x (Math/sin (+ x t))))
                  :width 64
                  :channels 2}])
         Curve (fn []
                 [:<>
                  [Data]
                  [mathbox.primitives/Line {:width 5
                                            :color "#3090FF"}]])
         Main (fn []
                [mathbox.core/MathBox
                 {:container {:style {:height "400px" :width "100%"}}
                  :focus 3}
                 [mathbox.primitives/Camera {:position [0 0 3]
                                             :proxy true}]
                 [mathbox.primitives/Cartesian
                  {:range [[-2 2] [-1 1]]
                   :scale [2 1]}
                  [mathbox.primitives/Axis {:axis 1 :width 3 :color "black"}]
                  [mathbox.primitives/Axis {:axis 2 :width 3 :color "black"}]
                  [mathbox.primitives/Grid {:width 2 :divideX 20 :divideY 10}]
                  [Curve]]])
         *state (r/atom {:open? false})]
     (fn []
       [:div
        [:button {:on-click #(swap! *state update :open? not)}
         (if (:open? @*state)
           "close"
           "open")]
        (when (:open? @*state)
          [Main])]))])

;; ### plotly
(kind/hiccup
 '[plotly
   {:data [{:x [0 1 3 2]
            :y [0 6 4 5]
            :z [0 8 9 7]
            :type :scatter3d
            :mode :lines+markers
            :opacity 0.5
            :line {:width 5}
            :marker {:size 4
                     :colorscale :Viridis}}]}])

;; ### Katex
(kind/hiccup
 '[katex "1+x^2"])

;; ### Emmy
(kind/hiccup
 '(require '[emmy.env :as e :refer [D]]))

(kind/hiccup
 [:div
  '(-> 'x
       ((D e/cube))
       e/simplify
       e/->infix)])

(kind/hiccup
 '[katex
   (-> 'x
       ((D e/cube))
       e/simplify
       e/->infix)])

;; ### tmdjs
(kind/hiccup
 '(require '[tech.v3.dataset :as tmd]))

(kind/hiccup
 '[(fn []
     [:div
      (-> {:x [1 2 3]}
          tmd/->dataset
          :x
          pr-str)])])


;; ### 3dmol

;; (experimental)

(kind/hiccup
 ['three-d-mol-viewer
  {:data-pdb "2POR"}])

(kind/hiccup
 ['(fn [{:keys [pdb-data]}]
     [:div
      {:style {:width "100%"
               :height "500px"
               :position "relative"}
       :ref (fn [el]
              (let [config (clj->js
                            {:backgroundColor "0xffffff"})
                    viewer (.createViewer js/$3Dmol el #_config)]
                (.setViewStyle viewer (clj->js
                                       {:style "outline"}))
                (.addModelsAsFrames viewer pdb-data "pdb")
                (.addSphere viewer (clj->js
                                    {:center {:x 0
                                              :y 0
                                              :z 0}
                                     :radius 1
                                     :color "green"}))
                (.zoomTo viewer)
                (.render viewer)
                (.zoom viewer 0.8 2000)))}
      ;; need to keep this symbol to let Clay infer the necessary dependency
      'three-d-mol])
  {:pdb-data (memoized-slurp "https://files.rcsb.org/download/2POR.pdb")}])

;; ### Leaflet

(kind/hiccup
 ['(fn []
     [:div
      {:style {:width "100%"
               :height "500px"}
       :ref (fn [el]
              (let [m (-> js/L
                          (.map el)
                          (.setView (clj->js [52.5274319 13.4004289])
                                    19))]
                (-> js/L
                    (.tileLayer "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
                                (clj->js {:maxZoom 19
                                          :attribution "&copy; <a href=\"http://www.openstreetmap.org/copyright\">OpenStreetMap</a>"}))
                    (.addTo m))))}
      'leaflet])])
