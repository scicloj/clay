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
 var32_line373
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :single-form '(+ 1 2)})))


(def var34_line381 (comment (clay/make! {:single-form '(+ 1 2)})))


(def var36_line387 (comment (clay/make! {:single-value 3})))


(def
 var38_line394
 (comment
  (clay/make!
   {:single-value 3333,
    :post-process (fn [html] (-> html (str/replace #"3333" "4444")))})))


(def
 var40_line403
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-ui-header true})))


(def
 var42_line410
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-info-line true})))


(def
 var44_line419
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def
 var46_line430
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def
 var48_line441
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def
 var50_line451
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def
 var52_line461
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def
 var54_line477
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
 var56_line490
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def
 var58_line496
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
 var60_line511
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example", :favicon "notebooks/favicon.ico"},
    :clean-up-target-dir true})))


(def
 var62_line525
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


(def var64_line541 (comment (clay/browse!)))


(def
 var66_line562
 (comment (clay/make-hiccup {:source-path "notebooks/index.clj"})))


(def var68_line577 scicloj.clay.v2.main/default-options)


(def var70_line610 scicloj.clay.v2.main/render-options)


(ns index-generated-test)


(def
 var74_line700
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


(def var75_line708 (->> (range 3) kind/fragment))


(def
 var77_line720
 (kind/fn {:x 1, :y 2} #:kindly{:f (fn [{:keys [x y]}] (+ x y))}))


(def
 var78_line725
 (kind/fn
  {:my-video-src
   "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  #:kindly{:f
           (fn
            [{:keys [my-video-src]}]
            (kind/video {:src my-video-src}))}))


(def var80_line732 (kind/fn [+ 1 2]))


(def
 var82_line737
 (kind/fn {:kindly/f (fn [{:keys [x y]}] (+ x y)), :x 1, :y 2}))


(def
 var84_line746
 (kind/fn
  {:x (range 3), :y (repeatedly 3 rand)}
  #:kindly{:f tc/dataset}))


(def
 var85_line751
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def
 var86_line756
 (kind/fn {:kindly/f tc/dataset, :x (range 3), :y (repeatedly 3 rand)}))


(def
 var88_line769
 (kind/echarts
  {:title {:text "Echarts Example"},
   :tooltip {:formatter #"(params) => 'hello: ' + params.name"},
   :legend {:data ["sales"]},
   :xAxis
   {:data ["Shirts" "Cardigans" "Chiffons" "Pants" "Heels" "Socks"]},
   :yAxis {},
   :series [{:name "sales", :type "bar", :data [5 20 36 10 10 20]}]}))


(def var90_line789 (delay (Thread/sleep 500) (+ 1 2)))


(def
 var92_line799
 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def var93_line802 (kind/image {:src "notebooks/images/Clay.svg.png"}))


(def
 var94_line805
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


(def var96_line830 (+ 1 2))


(deftest test97_line832 (is (> var96_line830 2.9)))


(deftest test98_line835 (is (> var96_line830 2.9)))


(deftest test99_line837 (is (> var96_line830 2.9)))


(def
 var101_line846
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def
 var103_line865
 (kind/table
  {:column-names ["A" "B" "C"], :row-vectors [[1 2 3] [4 5 6]]}
  {:class "table-responsive", :style {:background "#f8fff8"}}))


(def
 var105_line901
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :horizontal})
  false))


(def var106_line905 (+ 1 2))


(def var107_line907 (+ 3 4))


(def
 var109_line911
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :vertical})
  false))


(def var110_line915 (+ 1 2))


(def var111_line917 (+ 3 4))


(def
 var113_line921
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color "#ccddee"}})
  false))


(def var114_line925 (kind/hiccup [:div [:p "hello"]]))


(def var116_line932 (tc/dataset {:x (range 3)}))


(def var118_line936 (kind/hiccup [:div (tc/dataset {:x (range 3)})]))


(def
 var120_line942
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color nil}})
  false))


(def var121_line946 (kind/hiccup [:div [:p "hello"]]))
