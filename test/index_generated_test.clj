(ns
 index-generated-test
 (:require
  [clojure.java.io :as io]
  [clojure.edn :as edn]
  [scicloj.kindly.v4.kind :as kind]
  [tablecloth.api :as tc]
  [scicloj.kindly.v4.api :as kindly]
  [scicloj.clay.v2.api :as clay]
  [clojure.test :refer [deftest is]]))


(def
 var2_line14
 [:img
  {:style {:width "100px"},
   :src
   "https://raw.githubusercontent.com/scicloj/clay/main/resources/Clay.svg.png",
   :alt "Clay logo"}])


(def
 var4_line123
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
    "GsML75MtNXw"]
   ["Jan. 24th 2025"
    "Noj v2 - getting started - from raw data to a blog post (demonstrating CIDER integration and Quarto publishing)"
    "vnvcKtHHMVQ"]
   ["Mar 7th 2025"
    "Noj in a JAR - setup-free Clojure for beginners"
    "gHwFCOkBb_o"]
   ["Mar 7th 2025" "Noj Reload Executable" "tDz1x2d65C0"]
   ["Mar 24th 2025"
    "Clojure visual-tools 31 - Workflow Demos 5: Clay overview"
    "WiOUiHsq_dc"]
   ["May 2nd 2025"
    "Clojure for data analysis - getting started with Noj v2, VSCode, Calva, and Clay"
    "B1yPkpyiEEs"]]
  reverse
  (map
   (fn
    [[date title youtube-id]]
    [:tr [:td date] [:td title] [:td {:youtube-id youtube-id}]]))
  (into [:table])))


(def
 var6_line199
 (->
  "calva.exports/config.edn"
  io/resource
  slurp
  edn/read-string
  :customREPLCommandSnippets
  tc/dataset
  (tc/select-columns [:name :key])
  kind/table))


(ns
 index-generated-test
 (:require
  [scicloj.kindly.v4.api :as kindly]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.clay.v2.quarto.highlight-styles :as quarto.highlight-styles]
  [scicloj.clay.v2.quarto.themes :as quarto.themes]
  [scicloj.metamorph.ml.toydata :as toydata]
  [scicloj.tableplot.v1.hanami :as hanami]
  [scicloj.clay.v2.main]
  [tablecloth.api :as tc]
  [clojure.string :as str]
  [clojure.test :refer [deftest is]]))


(def
 var10_line277
 (kind/hiccup
  [:div
   {:style {:background "#efe9e6", :border-style :solid}}
   [:ul [:li "one"] [:li "two"] [:li "three"]]]))


(def
 var12_line286
 (->
  {:x (range 5), :y (repeatedly 5 rand)}
  tc/dataset
  (tc/set-dataset-name "my dataset")))


(def
 var14_line292
 (->
  (toydata/iris-ds)
  (hanami/plot
   hanami/rule-chart
   {:=x :sepal-width,
    :=x2 :sepal-length,
    :=y :petal-width,
    :=y2 :petal-length,
    :=color :species,
    :=color-type :nominal,
    :=mark-size 3,
    :=mark-opacity 0.2})))


(def var16_line305 (require '[scicloj.clay.v2.api :as clay]))


(def
 var18_line316
 (comment
  (clay/make! {:format [:html], :source-path "notebooks/index.clj"})))


(def
 var20_line322
 (comment (clay/make! {:source-path "notebooks/index.clj"})))


(def
 var22_line330
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :browse false})))


(def
 var24_line338
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :show false})))


(def
 var26_line345
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj",
    :favicon "notebooks/favicon.ico"})))


(def
 var28_line353
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :show false})))


(def
 var30_line363
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :live-reload true})))


(def
 var32_line372
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :live-reload :toggle})))


(def
 var34_line381
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :single-form '(+ 1 2)})))


(def var36_line389 (comment (clay/make! {:single-form '(+ 1 2)})))


(def var38_line395 (comment (clay/make! {:single-value 3})))


(def
 var40_line402
 (comment
  (clay/make!
   {:single-value 3333,
    :post-process (fn [html] (-> html (str/replace #"3333" "4444")))})))


(def
 var42_line411
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-ui-header true})))


(def
 var44_line418
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-info-line true})))


(def
 var46_line427
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def
 var48_line438
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def
 var50_line449
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def
 var52_line459
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def
 var54_line469
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def
 var56_line485
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


(def
 var58_line498
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def
 var60_line503
 (comment
  (clay/make! {:base-source-path "other_notebooks", :render true})))


(def
 var62_line509
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


(def
 var64_line524
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example", :favicon "notebooks/favicon.ico"},
    :clean-up-target-dir true})))


(def
 var66_line538
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path
    [{:part "Part A", :chapters ["index.clj" "chapter.clj"]}
     {:part "Part B", :chapters ["another_chapter.md"]}],
    :base-target-path "book",
    :book {:title "Book Example"},
    :clean-up-target-dir true})))


(def
 var68_line557
 (comment
  (clay/make!
   {:single-value
    (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}])})))


(def
 var70_line567
 (comment
  (clay/make!
   {:single-value (kind/hiccup [:img {:src "images/Clay.svg.png"}]),
    :keep-sync-root false})))


(def
 var72_line574
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path "demo.clj",
    :base-target-path "notebooks"})))


(def
 var74_line585
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path "demo.clj",
    :base-target-path "notebooks",
    :keep-sync-root false})))


(def
 var76_line596
 (comment
  (clay/make! {:source-path "notebooks/subdir/another_demo.clj"})))


(def
 var78_line603
 (comment
  (clay/make!
   {:source-path "notebooks/subdir/another_demo.clj",
    :flatten-targets false})))


(def
 var80_line610
 (comment
  (clay/make!
   {:source-path "notebooks/demo.clj",
    :flatten-targets false,
    :keep-sync-root false})))


(def var82_line618 (comment (clay/browse!)))


(def
 var84_line639
 (comment (clay/make-hiccup {:source-path "notebooks/index.clj"})))


(ns index-generated-test)


(def
 var88_line793
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


(def var89_line801 (->> (range 3) kind/fragment))


(def
 var91_line813
 (kind/fn {:x 1, :y 2} #:kindly{:f (fn [{:keys [x y]}] (+ x y))}))


(def
 var92_line818
 (kind/fn
  {:my-video-src
   "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  #:kindly{:f
           (fn
            [{:keys [my-video-src]}]
            (kind/video {:src my-video-src}))}))


(def var94_line825 (kind/fn [+ 1 2]))


(def
 var96_line830
 (kind/fn {:kindly/f (fn [{:keys [x y]}] (+ x y)), :x 1, :y 2}))


(def
 var98_line839
 (kind/fn
  {:x (range 3), :y (repeatedly 3 rand)}
  #:kindly{:f tc/dataset}))


(def
 var99_line844
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def
 var100_line849
 (kind/fn {:kindly/f tc/dataset, :x (range 3), :y (repeatedly 3 rand)}))


(def
 var102_line862
 (kind/echarts
  {:title {:text "Echarts Example"},
   :tooltip {:formatter #"(params) => 'hello: ' + params.name"},
   :legend {:data ["sales"]},
   :xAxis
   {:data ["Shirts" "Cardigans" "Chiffons" "Pants" "Heels" "Socks"]},
   :yAxis {},
   :series [{:name "sales", :type "bar", :data [5 20 36 10 10 20]}]}))


(def var104_line882 (delay (Thread/sleep 500) (+ 1 2)))


(def
 var106_line904
 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def var107_line907 (kind/image {:src "notebooks/images/Clay.svg.png"}))


(def
 var108_line910
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


(def var110_line935 (+ 1 2))


(deftest test111_line937 (is (> var110_line935 2.9)))


(deftest test112_line940 (is (> var110_line935 2.9)))


(deftest test113_line942 (is (> var110_line935 2.9)))


(def
 var115_line951
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def
 var117_line970
 (kind/table
  {:column-names ["A" "B" "C"], :row-vectors [[1 2 3] [4 5 6]]}
  {:class "table-responsive", :style {:background "#f8fff8"}}))


(def
 var119_line1006
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :horizontal})
  false))


(def var120_line1010 (+ 1 2))


(def var121_line1012 (+ 3 4))


(def
 var123_line1016
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :vertical})
  false))


(def var124_line1020 (+ 1 2))


(def var125_line1022 (+ 3 4))


(def
 var127_line1026
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color "#ccddee"}})
  false))


(def var128_line1030 (kind/hiccup [:div [:p "hello"]]))


(def var130_line1037 (tc/dataset {:x (range 3)}))


(def var132_line1041 (kind/hiccup [:div (tc/dataset {:x (range 3)})]))


(def
 var134_line1047
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color nil}})
  false))


(def var135_line1051 (kind/hiccup [:div [:p "hello"]]))
