(def var0_line1 nil)


(def
 var1_line5
 [:img
  {:style {:width "100px"},
   :src
   "https://raw.githubusercontent.com/scicloj/clay/main/resources/Clay.svg.png",
   :alt "Clay logo"}])


(def var2_line10 nil)


(def
 var3_line111
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


(def var4_line139 nil)


(def
 var5_line183
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:single-form '~form-before-caret\n                 :source-path [\"~file-path\"]}))"])


(def var6_line186 nil)


(def
 var7_line191
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:source-path [\"~file-path\"]}))"])


(def var8_line193 nil)


(ns
 index-generated-test
 (:require
  [scicloj.kindly.v4.api :as kindly]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.clay.v2.quarto.highlight-styles :as quarto.highlight-styles]
  [scicloj.clay.v2.quarto.themes :as quarto.themes]
  [scicloj.metamorph.ml.toydata :as toydata]
  [scicloj.tableplot.v1.hanami :as hanami]
  [tablecloth.api :as tc]
  [clojure.string :as str]
  [clojure.test :refer [deftest is]]))


(def var10_line216 nil)


(def
 var11_line217
 (kind/hiccup
  [:div
   {:style {:background "#efe9e6", :border-style :solid}}
   [:ul [:li "one"] [:li "two"] [:li "three"]]]))


(def var12_line225 nil)


(def
 var13_line226
 (->
  {:x (range 5), :y (repeatedly 5 rand)}
  tc/dataset
  (tc/set-dataset-name "my dataset")))


(def var14_line231 nil)


(def
 var15_line232
 (->
  (toydata/iris-ds)
  (hanami/plot
   hanami/rule-chart
   {:=x :sepal_width,
    :=x2 :sepal_length,
    :=y :petal_width,
    :=y2 :petal_length,
    :=color :species,
    :=color-type :nominal,
    :=mark-size 3,
    :=mark-opacity 0.2})))


(def var16_line243 nil)


(def var17_line245 (require '[scicloj.clay.v2.api :as clay]))


(def var18_line247 nil)


(def
 var19_line254
 (comment
  (clay/make! {:format [:html], :source-path "notebooks/index.clj"})))


(def var20_line258 nil)


(def
 var21_line260
 (comment (clay/make! {:source-path "notebooks/index.clj"})))


(def var22_line263 nil)


(def
 var23_line267
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :show false})))


(def var24_line271 nil)


(def
 var25_line274
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj",
    :favicon "notebooks/favicon.ico"})))


(def var26_line278 nil)


(def
 var27_line282
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :show false})))


(def var28_line287 nil)


(def
 var29_line292
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :live-reload true})))


(def var30_line298 nil)


(def
 var31_line302
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :single-form '(+ 1 2)})))


(def var32_line306 nil)


(def var33_line310 (comment (clay/make! {:single-form '(+ 1 2)})))


(def var34_line313 nil)


(def var35_line316 (comment (clay/make! {:single-value 3})))


(def var36_line319 nil)


(def
 var37_line323
 (comment
  (clay/make!
   {:single-value 3333,
    :post-process (fn [html] (-> html (str/replace #"3333" "4444")))})))


(def var38_line329 nil)


(def
 var39_line332
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-ui-header true})))


(def var40_line336 nil)


(def
 var41_line339
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-info-line true})))


(def var42_line343 nil)


(def
 var43_line348
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def var44_line352 nil)


(def
 var45_line359
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def var46_line364 nil)


(def
 var47_line370
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def var48_line374 nil)


(def
 var49_line380
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def var50_line384 nil)


(def
 var51_line390
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def var52_line396 nil)


(def
 var53_line406
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


(def var54_line414 nil)


(def
 var55_line419
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def var56_line423 nil)


(def
 var57_line425
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


(def var58_line438 nil)


(def
 var59_line440
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example", :favicon "notebooks/favicon.ico"},
    :clean-up-target-dir true})))


(def var60_line452 nil)


(def
 var61_line454
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


(def var62_line467 nil)


(def var63_line470 (comment (clay/browse!)))


(def var64_line473 nil)


(def
 var65_line491
 (comment (clay/make-hiccup {:source-path "notebooks/index.clj"})))


(def var66_line494 nil)


(def
 var67_line557
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


(def var68_line565 (->> (range 3) kind/fragment))


(def var69_line568 nil)


(def
 var70_line577
 (kind/fn {:x 1, :y 2} #:kindly{:f (fn [{:keys [x y]}] (+ x y))}))


(def
 var71_line582
 (kind/fn
  {:my-video-src
   "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  #:kindly{:f
           (fn
            [{:keys [my-video-src]}]
            (kind/video {:src my-video-src}))}))


(def var72_line587 nil)


(def var73_line589 (kind/fn [+ 1 2]))


(def var74_line592 nil)


(def
 var75_line594
 (kind/fn {:kindly/f (fn [{:keys [x y]}] (+ x y)), :x 1, :y 2}))


(def var76_line600 nil)


(def
 var77_line603
 (kind/fn
  {:x (range 3), :y (repeatedly 3 rand)}
  #:kindly{:f tc/dataset}))


(def
 var78_line608
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def
 var79_line613
 (kind/fn {:kindly/f tc/dataset, :x (range 3), :y (repeatedly 3 rand)}))


(def var80_line618 nil)


(def var81_line626 (delay (Thread/sleep 500) (+ 1 2)))


(def var82_line630 nil)


(def
 var83_line636
 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def var84_line639 (kind/image {:src "notebooks/images/Clay.svg.png"}))


(def
 var85_line642
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


(def var86_line654 nil)


(def var87_line667 (+ 1 2))


(deftest test88_line669 (is (> var87_line667 2.9)))


(deftest test89_line672 (is (> var87_line667 2.9)))


(deftest test90_line674 (is (> var87_line667 2.9)))


(def var91_line676 nil)


(def
 var92_line683
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def var93_line692 nil)


(def
 var94_line702
 (kind/table
  {:column-names ["A" "B" "C"], :row-vectors [[1 2 3] [4 5 6]]}
  {:class "table-responsive", :style {:background "#f8fff8"}}))


(def var95_line707 nil)


(def
 var96_line738
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :horizontal})
  false))


(def var97_line742 (+ 1 2))


(def var98_line744 (+ 3 4))


(def var99_line746 nil)


(def
 var100_line748
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :vertical})
  false))


(def var101_line752 (+ 1 2))


(def var102_line754 (+ 3 4))


(def var103_line756 nil)


(def
 var104_line758
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color "#ccddee"}})
  false))


(def var105_line762 (kind/hiccup [:div [:p "hello"]]))


(def var106_line766 nil)


(def var107_line769 (tc/dataset {:x (range 3)}))


(def var108_line771 nil)


(def var109_line773 (kind/hiccup [:div (tc/dataset {:x (range 3)})]))


(def var110_line777 nil)


(def
 var111_line779
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color nil}})
  false))


(def var112_line783 (kind/hiccup [:div [:p "hello"]]))
