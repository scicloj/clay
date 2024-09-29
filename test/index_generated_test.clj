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
 var3_line42
 ["(require '[scicloj.clay.v2.api :as clay])\n(clay/make! {:source-path \"notebooks/index.clj\"})"])


(def var4_line44 nil)


(def
 var5_line66
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


(def var6_line94 nil)


(def
 var7_line138
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:single-form '~form-before-caret\n                 :source-path [\"~file-path\"]}))"])


(def var8_line141 nil)


(def
 var9_line146
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:source-path [\"~file-path\"]}))"])


(def var10_line148 nil)


(ns
 index-generated-test
 (:require
  [scicloj.kindly.v4.api :as kindly]
  [scicloj.kindly.v4.kind :as kind]
  [scicloj.clay.v2.quarto.highlight-styles :as quarto.highlight-styles]
  [scicloj.clay.v2.quarto.themes :as quarto.themes]
  [scicloj.metamorph.ml.toydata :as toydata]
  [scicloj.hanamicloth.v1.api :as haclo]
  [tablecloth.api :as tc]
  [clojure.string :as str]
  [clojure.test :refer [deftest is]]))


(def var12_line171 nil)


(def
 var13_line172
 (kind/hiccup
  [:div
   {:style {:background "#efe9e6", :border-style :solid}}
   [:ul [:li "one"] [:li "two"] [:li "three"]]]))


(def var14_line180 nil)


(def
 var15_line181
 (->
  {:x (range 5), :y (repeatedly 5 rand)}
  tc/dataset
  (tc/set-dataset-name "my dataset")))


(def var16_line186 nil)


(def
 var17_line187
 (->
  (toydata/iris-ds)
  (haclo/plot
   haclo/rule-chart
   {:=x :sepal_width,
    :=x2 :sepal_length,
    :=y :petal_width,
    :=y2 :petal_length,
    :=color :species,
    :=color-type :nominal,
    :=mark-size 3,
    :=mark-opacity 0.2})))


(def var18_line198 nil)


(def var19_line200 (require '[scicloj.clay.v2.api :as clay]))


(def var20_line202 nil)


(def
 var21_line209
 (comment
  (clay/make! {:format [:html], :source-path "notebooks/index.clj"})))


(def var22_line213 nil)


(def
 var23_line215
 (comment (clay/make! {:source-path "notebooks/index.clj"})))


(def var24_line218 nil)


(def
 var25_line222
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :show false})))


(def var26_line226 nil)


(def
 var27_line229
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj",
    :favicon "notebooks/favicon.ico"})))


(def var28_line233 nil)


(def
 var29_line237
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :show false})))


(def var30_line242 nil)


(def
 var31_line246
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :single-form '(+ 1 2)})))


(def var32_line250 nil)


(def var33_line254 (comment (clay/make! {:single-form '(+ 1 2)})))


(def var34_line257 nil)


(def var35_line260 (comment (clay/make! {:single-value 3})))


(def var36_line263 nil)


(def
 var37_line267
 (comment
  (clay/make!
   {:single-value 3333,
    :post-process (fn [html] (-> html (str/replace #"3333" "4444")))})))


(def var38_line273 nil)


(def
 var39_line276
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-ui-header true})))


(def var40_line280 nil)


(def
 var41_line283
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-info-line true})))


(def var42_line287 nil)


(def
 var43_line292
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def var44_line296 nil)


(def
 var45_line303
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def var46_line308 nil)


(def
 var47_line314
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def var48_line318 nil)


(def
 var49_line324
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def var50_line328 nil)


(def
 var51_line334
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def var52_line340 nil)


(def
 var53_line350
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


(def var54_line358 nil)


(def
 var55_line363
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def var56_line367 nil)


(def
 var57_line369
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


(def var58_line382 nil)


(def
 var59_line384
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example", :favicon "notebooks/favicon.ico"},
    :clean-up-target-dir true})))


(def var60_line396 nil)


(def
 var61_line398
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


(def var62_line411 nil)


(def var63_line414 (comment (clay/browse!)))


(def var64_line417 nil)


(def
 var65_line423
 (comment (clay/make-hiccup {:source-path "notebooks/index.clj"})))


(def var66_line426 nil)


(def
 var67_line488
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


(def var68_line496 (->> (range 3) kind/fragment))


(def var69_line499 nil)


(def
 var70_line508
 (kind/fn {:x 1, :y 2} #:kindly{:f (fn [{:keys [x y]}] (+ x y))}))


(def
 var71_line513
 (kind/fn
  {:my-video-src
   "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  #:kindly{:f
           (fn
            [{:keys [my-video-src]}]
            (kind/video {:src my-video-src}))}))


(def var72_line518 nil)


(def var73_line520 (kind/fn [+ 1 2]))


(def var74_line523 nil)


(def
 var75_line525
 (kind/fn {:kindly/f (fn [{:keys [x y]}] (+ x y)), :x 1, :y 2}))


(def var76_line531 nil)


(def
 var77_line534
 (kind/fn
  {:x (range 3), :y (repeatedly 3 rand)}
  #:kindly{:f tc/dataset}))


(def
 var78_line539
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def
 var79_line544
 (kind/fn {:kindly/f tc/dataset, :x (range 3), :y (repeatedly 3 rand)}))


(def var80_line549 nil)


(def var81_line557 (delay (Thread/sleep 500) (+ 1 2)))


(def var82_line561 nil)


(def
 var83_line567
 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def var84_line570 (kind/image {:src "notebooks/images/Clay.svg.png"}))


(def
 var85_line573
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


(def var86_line585 nil)


(def var87_line598 (+ 1 2))


(deftest test88_line600 (is (> var87_line598 2.9)))


(deftest test89_line603 (is (> var87_line598 2.9)))


(deftest test90_line605 (is (> var87_line598 2.9)))


(def var91_line607 nil)


(def
 var92_line614
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def var93_line623 nil)


(def
 var94_line633
 (kind/table
  {:column-names ["A" "B" "C"], :row-vectors [[1 2 3] [4 5 6]]}
  {:class "table-responsive", :style {:background "#f8fff8"}}))


(def var95_line638 nil)


(def
 var96_line669
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :horizontal})
  false))


(def var97_line673 (+ 1 2))


(def var98_line675 (+ 3 4))


(def var99_line677 nil)


(def
 var100_line679
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :vertical})
  false))


(def var101_line683 (+ 1 2))


(def var102_line685 (+ 3 4))


(def var103_line687 nil)


(def
 var104_line689
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color "#ccddee"}})
  false))


(def var105_line693 (kind/hiccup [:div [:p "hello"]]))


(def var106_line697 nil)


(def var107_line700 (tc/dataset {:x (range 3)}))


(def var108_line702 nil)


(def var109_line704 (kind/hiccup [:div (tc/dataset {:x (range 3)})]))


(def var110_line708 nil)


(def
 var111_line710
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color nil}})
  false))


(def var112_line714 (kind/hiccup [:div [:p "hello"]]))
