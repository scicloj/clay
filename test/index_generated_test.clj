(def
 var0
 [:img
  {:style {:width "100px"},
   :src
   "https://raw.githubusercontent.com/scicloj/clay/main/resources/Clay.svg.png",
   :alt "Clay logo"}])


(def var1 nil)


(def
 var2
 ["(require '[scicloj.clay.v2.api :as clay])\n(clay/make! {:source-path \"notebooks/index.clj\"})"])


(def var3 nil)


(def
 var4
 (->>
  [["June 10th 2023" "An early overview - babashka-conf" "HvhMsv3iVGM"]
   ["Dec. 1st 2023"
    "Kindly & Clay overview - visual-tools group - see Daniel's & Tim's parts"
    "DAQnvAgBma8"]
   ["Dec. 12th 2023"
    "Demo & Clay overview - London Clojurians - see Tim's part"
    "skMMvxWjmNM"]
   ["Dec. 16th 2023" "Calva integration - datavis demo" "X_SsjhmG5Ok"]
   ["Dec. 17th 2023"
    "CIDER integration - image processing demo"
    "fd4kjlws6Ts"]
   ["Dec. 17th 2023"
    "Cursive integration, API, configuration - blogging demo"
    "GsML75MtNXw"]]
  reverse
  (map
   (fn
    [[date title youtube-id]]
    [:tr [:td date] [:td title] [:td {:youtube-id youtube-id}]]))
  (into [:table])))


(def var5 nil)


(def
 var6
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:single-form '~form-before-caret\n                 :source-path [\"~file-path\"]}))"])


(def var7 nil)


(def
 var8
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:source-path [\"~file-path\"]}))"])


(def var9 nil)


(ns
 index-generated-test
 (:require
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.kindly.v4.api :as kindly]
  [tablecloth.api :as tc]
  [scicloj.metamorph.ml :as ml]
  [scicloj.metamorph.ml.toydata :as toydata]
  [scicloj.noj.v1.stats :as noj.stats]
  [scicloj.noj.v1.vis.hanami :as hanami]
  [aerial.hanami.templates :as ht]
  [scicloj.clay.v2.quarto.themes :as quarto.themes]
  [scicloj.clay.v2.quarto.highlight-styles :as quarto.highlight-styles]
  [clojure.math :as math]
  [clojure.test :refer [deftest is]]))


(def var11 nil)


(def
 var12
 (kind/hiccup
  [:div
   {:style {:background "#efe9e6", :border-style :solid}}
   [:ul [:li "one"] [:li "two"] [:li "three"]]]))


(def var13 nil)


(def
 var14
 (->
  {:x (range 5), :y (repeatedly 5 rand)}
  tc/dataset
  (tc/set-dataset-name "my dataset")))


(def var15 nil)


(def
 var16
 (->
  (toydata/iris-ds)
  (hanami/plot
   ht/rule-chart
   {:X "sepal_width",
    :X2 "sepal_length",
    :Y "petal_width",
    :Y2 "petal_length",
    :COLOR "species",
    :SIZE 3,
    :OPACITY 0.2})))


(def var17 nil)


(def var18 (require '[scicloj.clay.v2.api :as clay]))


(def var19 nil)


(def
 var20
 (comment
  (clay/make! {:format [:html], :source-path "notebooks/index.clj"})))


(def var21 nil)


(def var22 (comment (clay/make! {:source-path "notebooks/index.clj"})))


(def var23 nil)


(def
 var24
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :show false})))


(def var25 nil)


(def
 var26
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :show false})))


(def var27 nil)


(def
 var28
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj",
    :single-form
    '(kind/cytoscape
      cytoscape-example
      #:element{:style {:width "300px", :height "300px"}})})))


(def var29 nil)


(def
 var30
 (comment
  (clay/make!
   {:single-form
    '(kind/cytoscape
      cytoscape-example
      #:element{:style {:width "300px", :height "300px"}})})))


(def var31 nil)


(def
 var32
 (comment
  (clay/make!
   {:single-value
    (kind/cytoscape
     cytoscape-example
     #:element{:style {:width "300px", :height "300px"}})})))


(def var33 nil)


(def
 var34
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def var35 nil)


(def
 var36
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def var37 nil)


(def
 var38
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def var39 nil)


(def
 var40
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def var41 nil)


(def
 var42
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def var43 nil)


(def
 var44
 (comment
  (require
   '[scicloj.clay.v2.quarto.highlight-styles
     :as
     quarto.highlight-styles]
   '[scicloj.clay.v2.quarto.themes :as quarto.themes])
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style quarto.highlight-styles/nord,
     :format {:html {:theme quarto.themes/journal}}}})))


(def var45 nil)


(def
 var46
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def var47 nil)


(def
 var48
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path
    ["chapter.clj"
     "another_chapter.md"
     "a_chapter_with_R_code.Rmd"
     "test.ipynb"],
    :base-target-path "book",
    :book {:title "Book Example"},
    :clean-up-target-dir true})))


(def var49 nil)


(def
 var50
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example"},
    :clean-up-target-dir true})))


(def var51 nil)


(def var52 (comment (clay/browse!)))


(def var53 nil)


(def var54 (+ 4 5))


(def var55 (str "abcd" "efgh"))


(def var56 nil)


(def var57 (kind/hidden 9))


(def var58 nil)


(def
 var59
 (kind/hiccup
  [:ul
   [:li [:p "hi"]]
   [:li [:big [:big [:p {:style {:color "#7F5F3F"}} "hello"]]]]]))


(def var60 nil)


(def
 var61
 (kind/reagent
  ['(fn
     [numbers]
     [:p {:style {:background "#d4ebe9"}} (pr-str (map inc numbers))])
   (vec (range 40))]))


(def var62 nil)


(def
 var63
 (kind/reagent
  ['(fn
     []
     (let
      [*click-count (reagent.core/atom 0)]
      (fn
       []
       [:div
        "The atom "
        [:code "*click-count"]
        " has value: "
        @*click-count
        ". "
        [:input
         {:type "button",
          :value "Click me!",
          :on-click (fn* [] (swap! *click-count inc))}]])))]))


(def var64 nil)


(def
 var65
 (kind/html
  "<div style='height:40px; width:40px; background:purple'></div> "))


(def
 var66
 (kind/html
  "\n<svg height=100 width=100>\n<circle cx=50 cy=50 r=40 stroke='purple' stroke-width=3 fill='floralwhite' />\n</svg> "))


(def var67 nil)


(def
 var68
 (kind/md "This is [markdown](https://www.markdownguide.org/)."))


(def
 var69
 (kind/md
  ["\n* This is [markdown](https://www.markdownguide.org/).\n  * *Isn't it??*"
   "\n* Here is **some more** markdown."]))


(def var70 nil)


(def var71 (kind/md "Let $x=9$. Then $$x+11=20$$"))


(def var72 nil)


(def var73 (import javax.imageio.ImageIO java.net.URL))


(def
 var74
 (defonce
  clay-image
  (->
   "https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg"
   (URL.)
   (ImageIO/read))))


(def var75 clay-image)


(def var76 nil)


(def
 var77
 (def
  people-as-maps
  (->>
   (range 29)
   (mapv
    (fn
     [i]
     {:preferred-language
      (["clojure" "clojurescript" "babashka"] (rand-int 3)),
      :age (rand-int 100)})))))


(def
 var78
 (def
  people-as-vectors
  (->> people-as-maps (mapv (juxt :preferred-language :age)))))


(def var79 (take 5 people-as-maps))


(def var80 (take 5 people-as-vectors))


(def var81 (->> people-as-vectors (take 5) set))


(def var82 nil)


(def
 var83
 (def
  nested-structure-1
  {:vector-of-numbers [2 9 -1],
   :vector-of-different-things
   ["hi" (kind/hiccup [:big [:big "hello"]])],
   :map-of-different-things
   {:markdown (kind/md ["*hi*, **hi**"]), :number 9999},
   :hiccup (kind/hiccup [:big [:big "bye"]]),
   :dataset (tc/dataset {:x (range 3), :y [:A :B :C]})}))


(def var84 nested-structure-1)


(def var85 nil)


(def var86 (kind/pprint nested-structure-1))


(def var87 nil)


(def var88 (require '[tablecloth.api :as tc]))


(def var89 (-> {:x (range 6), :y [:A :B :C :A :B :C]} tc/dataset))


(def var90 (-> {:x [1 [2 3] 4], :y [:A :B :C]} tc/dataset))


(def var91 (-> [{:x 1, :y 2, :z 3} {:y 4, :z 5}] tc/dataset))


(def var92 (-> people-as-maps tc/dataset))


(def var93 nil)


(def
 var94
 (kind/table
  {:column-names [:preferred-language :age],
   :row-vectors people-as-vectors}))


(def var95 nil)


(def var96 (kind/table {:row-vectors (take 5 people-as-vectors)}))


(def var97 nil)


(def var98 (kind/table {:row-maps (take 5 people-as-maps)}))


(def var99 nil)


(def
 var100
 (kind/table
  {:column-names [:preferred-language],
   :row-maps (take 5 people-as-maps)}))


(def var101 nil)


(def var102 (kind/table (take 5 people-as-vectors)))


(def var103 (kind/table (take 5 people-as-maps)))


(def var104 nil)


(def var105 (kind/table {:x (range 6), :y [:A :B :C :A :B :C]}))


(def var106 nil)


(def var107 (def people-as-dataset (tc/dataset people-as-maps)))


(def var108 (-> people-as-dataset kind/table))


(def var109 nil)


(def
 var110
 (-> people-as-dataset (kind/table #:element{:max-height "300px"})))


(def var111 nil)


(def
 var112
 (-> people-as-maps tc/dataset (kind/table {:use-datatables true})))


(def
 var113
 (->
  people-as-dataset
  (kind/table {:use-datatables true, :datatables {:scrollY 300}})))


(def var114 nil)


(def
 var115
 (->
  (toydata/iris-ds)
  (noj.stats/linear-regression-model
   :sepal_length
   [:sepal_width :petal_width :petal_length])
  ml/thaw-model))


(def var116 nil)


(def
 var117
 (defn
  vega-lite-point-plot
  [data]
  (->
   {:data {:values data},
    :mark "point",
    :encoding
    {:size {:field "w", :type "quantitative"},
     :x {:field "x", :type "quantitative"},
     :y {:field "y", :type "quantitative"},
     :fill {:field "z", :type "nominal"}}}
   kind/vega-lite)))


(def
 var118
 (defn
  random-data
  [n]
  (->>
   (repeatedly n (fn* [] (- (rand) 0.5)))
   (reductions +)
   (map-indexed
    (fn [x y] {:w (rand-int 9), :z (rand-int 9), :x x, :y y})))))


(def
 var119
 (defn
  random-vega-lite-plot
  [n]
  (-> n random-data vega-lite-point-plot)))


(def var120 (random-vega-lite-plot 9))


(def var121 nil)


(def
 var122
 (->
  {:data
   {:values "x,y\n1,1\n2,4\n3,9\n-1,1\n-2,4\n-3,9",
    :format {:type :csv}},
   :mark "point",
   :encoding
   {:x {:field "x", :type "quantitative"},
    :y {:field "y", :type "quantitative"}}}
  kind/vega-lite))


(def var123 nil)


(def
 var124
 (def
  cytoscape-example
  {:elements
   {:nodes
    [{:data {:id "a", :parent "b"}, :position {:x 215, :y 85}}
     {:data {:id "b"}}
     {:data {:id "c", :parent "b"}, :position {:x 300, :y 85}}
     {:data {:id "d"}, :position {:x 215, :y 175}}
     {:data {:id "e"}}
     {:data {:id "f", :parent "e"}, :position {:x 300, :y 175}}],
    :edges
    [{:data {:id "ad", :source "a", :target "d"}}
     {:data {:id "eb", :source "e", :target "b"}}]},
   :style
   [{:selector "node",
     :css
     {:content "data(id)",
      :text-valign "center",
      :text-halign "center"}}
    {:selector "parent",
     :css {:text-valign "top", :text-halign "center"}}
    {:selector "edge",
     :css {:curve-style "bezier", :target-arrow-shape "triangle"}}],
   :layout {:name "preset", :padding 5}}))


(def var125 (kind/cytoscape cytoscape-example))


(def
 var126
 (->
  cytoscape-example
  (kind/cytoscape #:element{:style {:width "100px", :height "100px"}})))


(def var127 nil)


(def
 var128
 (def
  echarts-example
  {:title {:text "Echarts Example"},
   :tooltip {},
   :legend {:data ["sales"]},
   :xAxis
   {:data ["Shirts" "Cardigans" "Chiffons" "Pants" "Heels" "Socks"]},
   :yAxis {},
   :series [{:name "sales", :type "bar", :data [5 20 36 10 10 20]}]}))


(def var129 (kind/echarts echarts-example))


(def
 var130
 (->
  echarts-example
  (kind/echarts #:element{:style {:width "500px", :height "200px"}})))


(def var131 nil)


(def
 var132
 (def
  plotly-example
  {:data
   [{:x [0 1 3 2],
     :y [0 6 4 5],
     :z [0 8 9 7],
     :type :scatter3d,
     :mode :lines+markers,
     :opacity 0.5,
     :line {:width 5},
     :marker {:size 4, :colorscale :Viridis}}],
   :layout {:title "Plotly example"}}))


(def var133 (kind/plotly plotly-example))


(def
 var134
 (->
  plotly-example
  (kind/plotly #:element{:style {:width "300px", :height "300px"}})))


(def var135 nil)


(def
 var136
 (kind/highcharts
  {:title {:text "Line chart"},
   :subtitle {:text "By Job Category"},
   :yAxis {:title {:text "Number of Employees"}},
   :series
   [{:name "Installation & Developers",
     :data
     [43934
      48656
      65165
      81827
      112143
      142383
      171533
      165174
      155157
      161454
      154610]}
    {:name "Manufacturing",
     :data
     [24916
      37941
      29742
      29851
      32490
      30282
      38121
      36885
      33726
      34243
      31050]}
    {:name "Sales & Distribution",
     :data
     [11744
      30000
      16005
      19771
      20185
      24377
      32147
      30912
      29243
      29213
      25663]}
    {:name "Operations & Maintenance",
     :data [nil nil nil nil nil nil nil nil 11164 11218 10077]}
    {:name "Other",
     :data
     [21908
      5548
      8105
      11248
      8989
      11816
      18274
      17300
      13053
      11906
      10073]}],
   :xAxis {:accessibility {:rangeDescription "Range: 2010 to 2020"}},
   :legend
   {:layout "vertical", :align "right", :verticalAlign "middle"},
   :plotOptions
   {:series {:label {:connectorAllowed false}, :pointStart 2010}},
   :responsive
   {:rules
    [{:condition {:maxWidth 500},
      :chartOptions
      {:legend
       {:layout "horizontal",
        :align "center",
        :verticalAlign "bottom"}}}]}}))


(def var137 nil)


(def
 var138
 (kind/observable
  "\n//| panel: input\nviewof bill_length_min = Inputs.range(\n                                      [32, 50],\n                                      {value: 35, step: 1, label: 'Bill length (min):'}\n                                      )\nviewof islands = Inputs.checkbox(\n                                 ['Torgersen', 'Biscoe', 'Dream'],\n                                 { value: ['Torgersen', 'Biscoe'],\n                                  label: 'Islands:'\n                                  }\n                                 )\n\nPlot.rectY(filtered,\n            Plot.binX(\n                      {y: 'count'},\n                      {x: 'body_mass_g', fill: 'species', thresholds: 20}\n                      ))\n .plot({\n        facet: {\n                data: filtered,\n                x: 'sex',\n                y: 'species',\n                marginRight: 80\n                },\n        marks: [\n                Plot.frame(),\n                ]\n        }\n       )\nInputs.table(filtered)\npenguins = FileAttachment('notebooks/datasets/palmer-penguins.csv').csv({ typed: true })\nfiltered = penguins.filter(function(penguin) {\n                                           return bill_length_min < penguin.bill_length_mm &&\n                                           islands.includes(penguin.island);\n                                           })\n"))


(def var139 nil)


(def
 var140
 (kind/observable
  "athletes = FileAttachment('notebooks/datasets/athletes.csv').csv({typed: true})"))


(def var141 (kind/observable "athletes"))


(def var142 (kind/observable "Inputs.table(athletes)"))


(def
 var143
 (kind/observable
  "\nPlot.plot({\n  grid: true,\n  facet: {\n    data: athletes,\n    y: 'sex'\n  },\n  marks: [\n    Plot.rectY(\n      athletes,\n      Plot.binX({y: 'count'}, {x: 'weight', fill: 'sex'})\n    ),\n    Plot.ruleY([0])\n  ]\n})\n"))


(def
 var144
 (kind/observable
  "population = FileAttachment('notebooks/datasets/population.json').json()"))


(def var145 (kind/observable "population"))


(def
 var146
 (kind/observable
  " import { chart } with { population as data } from '@d3/zoomable-sunburst'\n chart"))


(def var147 nil)


(def
 var148
 (kind/reagent
  ['(fn
     []
     [:div
      {:style {:height "200px"},
       :ref
       (fn
        [el]
        (let
         [m (-> js/L (.map el) (.setView (clj->js [51.505 -0.09]) 13))]
         (->
          js/L
          .-tileLayer
          (.provider "OpenStreetMap.Mapnik")
          (.addTo m))
         (->
          js/L
          (.marker (clj->js [51.5 -0.09]))
          (.addTo m)
          (.bindPopup "A pretty CSS popup.<br> Easily customizable.")
          (.openPopup))))}])]
  #:html{:deps [:leaflet]}))


(def var149 nil)


(def
 var150
 (let
  [letter-frequencies
   [{:letter "A", :frequency 0.08167}
    {:letter "B", :frequency 0.01492}
    {:letter "C", :frequency 0.02782}
    {:letter "D", :frequency 0.04253}
    {:letter "E", :frequency 0.12702}
    {:letter "F", :frequency 0.02288}
    {:letter "G", :frequency 0.02015}
    {:letter "H", :frequency 0.06094}
    {:letter "I", :frequency 0.06966}
    {:letter "J", :frequency 0.00153}
    {:letter "K", :frequency 0.00772}
    {:letter "L", :frequency 0.04025}
    {:letter "M", :frequency 0.02406}
    {:letter "N", :frequency 0.06749}
    {:letter "O", :frequency 0.07507}
    {:letter "P", :frequency 0.01929}
    {:letter "Q", :frequency 9.5E-4}
    {:letter "R", :frequency 0.05987}
    {:letter "S", :frequency 0.06327}
    {:letter "T", :frequency 0.09056}
    {:letter "U", :frequency 0.02758}
    {:letter "V", :frequency 0.00978}
    {:letter "W", :frequency 0.0236}
    {:letter "X", :frequency 0.0015}
    {:letter "Y", :frequency 0.01974}
    {:letter "Z", :frequency 7.4E-4}]]
  (kind/reagent
   ['(fn
      [data]
      (let
       [size
        400
        x
        (->
         js/d3
         .scaleLinear
         (.range (into-array [0 size]))
         (.domain (into-array [0 (apply max (map :frequency data))])))
        y
        (->
         js/d3
         .scaleBand
         (.domain (into-array (map :letter data)))
         (.range (into-array [0 size])))
        color
        (.scaleOrdinal js/d3 (.-schemeCategory10 js/d3))]
       [:svg
        {:viewBox (str "0 0 " size " " size)}
        (map
         (fn
          [{:keys [letter frequency]}]
          [:g
           {:key letter,
            :transform (str "translate(" 0 "," (y letter) ")")}
           [:rect
            {:x (x 0),
             :height (.bandwidth y),
             :fill (color letter),
             :width (x frequency)}]])
         data)]))
    letter-frequencies]
   #:html{:deps [:d3]})))


(def var151 nil)


(def
 var152
 (defn
  ->ggplotly-spec
  [{:keys [layers labels]}]
  (kind/htmlwidgets-ggplotly
   (let
    [{:keys [data xmin xmax ymin ymax]}
     (first layers)
     ->tickvals
     (fn
      [l r]
      (let
       [jump (-> (- r l) (/ 6) math/floor int (max 1))]
       (-> l math/ceil (range r jump))))]
    {:x
     {:config
      {:doubleClick "reset",
       :modeBarButtonsToAdd ["hoverclosest" "hovercompare"],
       :showSendToCloud false},
      :layout
      {:plot_bgcolor "rgba(235,235,235,1)",
       :paper_bgcolor "rgba(255,255,255,1)",
       :legend
       {:bgcolor "rgba(255,255,255,1)",
        :bordercolor "transparent",
        :borderwidth 1.88976377952756,
        :font
        {:color "rgba(0,0,0,1)", :family "", :size 11.689497716895}},
       :xaxis
       (let
        [tickvals
         (->tickvals xmin xmax)
         ticktext
         (mapv str tickvals)
         range-len
         (- xmax xmin)
         range-expansion
         (* 0.1 range-len)
         expanded-range
         [(- xmin range-expansion) (+ xmax range-expansion)]]
        {:linewidth 0,
         :nticks nil,
         :linecolor nil,
         :ticklen 3.65296803652968,
         :tickcolor "rgba(51,51,51,1)",
         :tickmode "array",
         :gridcolor "rgba(255,255,255,1)",
         :automargin true,
         :type "linear",
         :tickvals tickvals,
         :zeroline false,
         :title
         {:text (:x labels),
          :font
          {:color "rgba(0,0,0,1)",
           :family "",
           :size 14.6118721461187}},
         :tickfont
         {:color "rgba(77,77,77,1)",
          :family "",
          :size 11.689497716895},
         :autorange false,
         :showticklabels true,
         :showline false,
         :showgrid true,
         :ticktext ticktext,
         :ticks "outside",
         :gridwidth 0.66417600664176,
         :anchor "y",
         :domain [0 1],
         :hoverformat ".2f",
         :tickangle 0,
         :tickwidth 0.66417600664176,
         :categoryarray ticktext,
         :categoryorder "array",
         :range expanded-range}),
       :font
       {:color "rgba(0,0,0,1)", :family "", :size 14.6118721461187},
       :showlegend false,
       :barmode "relative",
       :yaxis
       (let
        [tickvals
         (->tickvals ymin ymax)
         ticktext
         (mapv str tickvals)
         range-len
         (- ymax ymin)
         range-expansion
         (* 0.1 range-len)
         expanded-range
         [(- ymin range-expansion) (+ ymax range-expansion)]]
        {:linewidth 0,
         :nticks nil,
         :linecolor nil,
         :ticklen 3.65296803652968,
         :tickcolor "rgba(51,51,51,1)",
         :tickmode "array",
         :gridcolor "rgba(255,255,255,1)",
         :automargin true,
         :type "linear",
         :tickvals tickvals,
         :zeroline false,
         :title
         {:text (:y labels),
          :font
          {:color "rgba(0,0,0,1)",
           :family "",
           :size 14.6118721461187}},
         :tickfont
         {:color "rgba(77,77,77,1)",
          :family "",
          :size 11.689497716895},
         :autorange false,
         :showticklabels true,
         :showline false,
         :showgrid true,
         :ticktext ticktext,
         :ticks "outside",
         :gridwidth 0.66417600664176,
         :anchor "x",
         :domain [0 1],
         :hoverformat ".2f",
         :tickangle 0,
         :tickwidth 0.66417600664176,
         :categoryarray ticktext,
         :categoryorder "array",
         :range expanded-range}),
       :hovermode "closest",
       :margin
       {:t 25.7412480974125,
        :r 7.30593607305936,
        :b 39.6955859969559,
        :l 37.2602739726027},
       :shapes
       [{:yref "paper",
         :fillcolor nil,
         :xref "paper",
         :y1 1,
         :type "rect",
         :line {:color nil, :width 0, :linetype []},
         :y0 0,
         :x1 1,
         :x0 0}]},
      :highlight
      {:on "plotly_click",
       :persistent false,
       :dynamic false,
       :selectize false,
       :opacityDim 0.2,
       :selected {:opacity 1},
       :debounce 0},
      :base_url "https://plot.ly",
      :cur_data "1f2fea5b54d146",
      :source "A",
      :shinyEvents
      ["plotly_hover"
       "plotly_click"
       "plotly_selected"
       "plotly_relayout"
       "plotly_brushed"
       "plotly_brushing"
       "plotly_clickannotation"
       "plotly_doubleclick"
       "plotly_deselect"
       "plotly_afterplot"
       "plotly_sunburstclick"],
      :attrs {:1f2fea5b54d146 {:x {}, :y {}, :type "scatter"}},
      :data
      [{:y (:y data),
        :hoveron "points",
        :frame nil,
        :hoverinfo "text",
        :marker
        {:autocolorscale false,
         :color "rgba(0,0,0,1)",
         :opacity 1,
         :size 5.66929133858268,
         :symbol "circle",
         :line {:width 1.88976377952756, :color "rgba(0,0,0,1)"}},
        :mode "markers",
        :type "scatter",
        :xaxis "x",
        :showlegend false,
        :yaxis "y",
        :x (:x data),
        :text
        (->
         data
         (tc/select-columns [:x :y])
         (tc/rows :as-maps)
         (->> (mapv pr-str)))}]},
     :evals [],
     :jsHooks []}))))


(def var153 (require '[tech.v3.datatype.functional :as fun]))


(def var154 nil)


(def
 var155
 (let
  [n
   100
   xs
   (range n)
   ys
   (reductions + (repeatedly n (fn* [] (- (rand) 0.5))))
   xmin
   (fun/reduce-min xs)
   xmax
   (fun/reduce-max xs)
   ymin
   (fun/reduce-min ys)
   ymax
   (fun/reduce-max ys)
   data
   (tc/dataset {:x xs, :y ys})]
  (->ggplotly-spec
   {:layers
    [{:data data, :xmin xmin, :xmax xmax, :ymin ymin, :ymax ymax}],
    :labels {:x "wt", :y "mpg"}})))


(def var156 nil)


(def
 var157
 (kind/reagent
  ['(fn
     [{:keys [data-pdb]}]
     [:div
      {:style {:height "400px", :width "400px", :position :relative},
       :class "viewer_3Dmoljs",
       :data-pdb data-pdb,
       :data-backgroundcolor "0xffffff",
       :data-style "stick",
       :data-ui true}])
   {:data-pdb "2POR"}]
  #:html{:deps [:three-d-mol]}))


(def var158 nil)


(def
 var159
 (defonce pdb-2POR (slurp "https://files.rcsb.org/download/2POR.pdb")))


(def
 var160
 (kind/reagent
  ['(fn
     [{:keys [pdb-data]}]
     [:div
      {:style {:width "100%", :height "500px", :position "relative"},
       :ref
       (fn
        [el]
        (let
         [config
          (clj->js {:backgroundColor "0xffffff"})
          viewer
          (.createViewer js/$3Dmol el)]
         (.setViewStyle viewer (clj->js {:style "outline"}))
         (.addModelsAsFrames viewer pdb-data "pdb")
         (.addSphere
          viewer
          (clj->js
           {:center {:x 0, :y 0, :z 0},
            :radius 5,
            :color "green",
            :alpha 0.2}))
         (.zoomTo viewer)
         (.render viewer)
         (.zoom viewer 0.8 2000)))}])
   {:pdb-data pdb-2POR}]
  #:html{:deps [:three-d-mol]}))


(def var161 nil)


(def var162 (kind/video {:youtube-id "DAQnvAgBma8"}))


(def
 var163
 (kind/video {:youtube-id "DAQnvAgBma8", :allowfullscreen false}))


(def
 var164
 (kind/video
  {:youtube-id "DAQnvAgBma8", :iframe-width 480, :iframe-height 270}))


(def
 var165
 (kind/video
  {:youtube-id "DAQnvAgBma8", :embed-options {:mute 1, :controls 0}}))


(def var166 nil)


(def var167 (kind/portal {:x (range 3)}))


(def var168 nil)


(def
 var169
 (kind/portal
  [(kind/hiccup
    [:img
     {:height 50,
      :width 50,
      :src "https://clojure.org/images/clojure-logo-120b.png"}])
   (kind/hiccup
    [:img
     {:height 50,
      :width 50,
      :src
      "https://raw.githubusercontent.com/djblue/portal/fbc54632adc06c6e94a3d059c858419f0063d1cf/resources/splash.svg"}])]))


(def
 var170
 (kind/portal
  [(kind/hiccup [:big [:big "a plot"]]) (random-vega-lite-plot 9)]))


(def var171 nil)


(def
 var172
 (kind/hiccup
  [:div
   {:style {:background "#f5f3ff", :border "solid"}}
   [:hr]
   [:pre [:code "kind/md"]]
   (kind/md "*some text* **some more text**")
   [:hr]
   [:pre [:code "kind/code"]]
   (kind/code "{:x (1 2 [3 4])}")
   [:hr]
   [:pre [:code "kind/dataset"]]
   (tc/dataset {:x (range 33), :y (map inc (range 33))})
   [:hr]
   [:pre [:code "kind/table"]]
   (kind/table (tc/dataset {:x (range 33), :y (map inc (range 33))}))
   [:hr]
   [:pre [:code "kind/vega-lite"]]
   (random-vega-lite-plot 9)
   [:hr]
   [:pre [:code "kind/vega-lite"]]
   (->
    {:data
     {:values "x,y\n1,1\n2,4\n3,9\n-1,1\n-2,4\n-3,9",
      :format {:type :csv}},
     :mark "point",
     :encoding
     {:x {:field "x", :type "quantitative"},
      :y {:field "y", :type "quantitative"}}}
    kind/vega-lite)
   [:hr]
   [:pre [:code "kind/reagent"]]
   (kind/reagent
    ['(fn
       [numbers]
       [:p
        {:style {:background "#d4ebe9"}}
        (pr-str (map inc numbers))])
     (vec (range 40))])]))


(def var173 nil)


(def
 var174
 (kind/table
  {:column-names
   [(kind/hiccup
     [:div {:style {:background-color "#ccaabb"}} [:big ":x"]])
    (kind/hiccup
     [:div {:style {:background-color "#aabbcc"}} [:big ":y"]])],
   :row-vectors
   [[(kind/md "*some text* **some more text**")
     (kind/code "{:x (1 2 [3 4])}")]
    [(tc/dataset {:x (range 3), :y (map inc (range 3))})
     (random-vega-lite-plot 9)]
    [(kind/hiccup [:div.clay-limit-image-width clay-image])
     (kind/md "$x^2$")]]}))


(def
 var175
 (kind/table
  {:column-names ["size" "square"],
   :row-vectors
   (for
    [i (range 20)]
    (let
     [size (* i 10) px (str size "px")]
     [size
      (kind/hiccup
       [:div
        {:style
         {:height px, :width px, :background-color "purple"}}])]))}
  {:use-datatables true}))


(def var176 nil)


(def
 var177
 {:plot (random-vega-lite-plot 9),
  :dataset (tc/dataset {:x (range 3), :y (repeatedly 3 rand)})})


(def
 var178
 [(random-vega-lite-plot 9)
  (tc/dataset {:x (range 3), :y (repeatedly 3 rand)})])


(def var179 nil)


(def
 var180
 (->>
  ["purple" "darkgreen" "brown"]
  (mapcat
   (fn
    [color]
    [(kind/md (str "### subsection: " color))
     (kind/hiccup
      [:div
       {:style {:background-color color, :color "lightgrey"}}
       [:big [:p color]]])]))
  kind/fragment))


(def var181 (->> (range 3) kind/fragment))


(def var182 nil)


(def var183 (kind/fn [+ 1 2]))


(def
 var184
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def var185 nil)


(def var186 (delay (Thread/sleep 500) (+ 1 2)))


(def var187 nil)


(def var188 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def
 var189
 (kind/vega-lite
  {:data {:url "notebooks/datasets/iris.csv"},
   :mark "rule",
   :encoding
   {:opacity {:value 0.2},
    :size {:value 3},
    :x {:field "sepal_width", :type "quantitative"},
    :x2 {:field "sepal_length", :type "quantitative"},
    :y {:field "petal_width", :type "quantitative"},
    :y2 {:field "petal_length", :type "quantitative"},
    :color {:field "species", :type "nominal"}},
   :background "floralwhite"}))


(def var190 nil)


(def var191 (+ 1 2))


(deftest test192 (is (> var191 2.9)))


(deftest test193 (is (> var191 2.9)))


(deftest test194 (is (> var191 2.9)))


(def var195 nil)


(def
 var196
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def var197 nil)
