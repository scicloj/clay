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
 var3_line110
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


(def var4_line138 nil)


(def
 var5_line184
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:single-form '~form-before-caret\n                 :source-path [\"~file-path\"]}))"])


(def var6_line187 nil)


(def
 var7_line192
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:source-path [\"~file-path\"]}))"])


(def var8_line194 nil)


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


(def var10_line217 nil)


(def
 var11_line218
 (kind/hiccup
  [:div
   {:style {:background "#efe9e6", :border-style :solid}}
   [:ul [:li "one"] [:li "two"] [:li "three"]]]))


(def var12_line226 nil)


(def
 var13_line227
 (->
  {:x (range 5), :y (repeatedly 5 rand)}
  tc/dataset
  (tc/set-dataset-name "my dataset")))


(def var14_line232 nil)


(def
 var15_line233
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


(def var16_line244 nil)


(def var17_line246 (require '[scicloj.clay.v2.api :as clay]))


(def var18_line248 nil)


(def
 var19_line255
 (comment
  (clay/make! {:format [:html], :source-path "notebooks/index.clj"})))


(def var20_line259 nil)


(def
 var21_line261
 (comment (clay/make! {:source-path "notebooks/index.clj"})))


(def var22_line264 nil)


(def
 var23_line268
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :show false})))


(def var24_line272 nil)


(def
 var25_line275
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj",
    :favicon "notebooks/favicon.ico"})))


(def var26_line279 nil)


(def
 var27_line283
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :show false})))


(def var28_line288 nil)


(def
 var29_line293
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :live-reload true})))


(def var30_line299 nil)


(def
 var31_line303
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :single-form '(+ 1 2)})))


(def var32_line307 nil)


(def var33_line311 (comment (clay/make! {:single-form '(+ 1 2)})))


(def var34_line314 nil)


(def var35_line317 (comment (clay/make! {:single-value 3})))


(def var36_line320 nil)


(def
 var37_line324
 (comment
  (clay/make!
   {:single-value 3333,
    :post-process (fn [html] (-> html (str/replace #"3333" "4444")))})))


(def var38_line330 nil)


(def
 var39_line333
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-ui-header true})))


(def var40_line337 nil)


(def
 var41_line340
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-info-line true})))


(def var42_line344 nil)


(def
 var43_line349
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def var44_line353 nil)


(def
 var45_line360
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def var46_line365 nil)


(def
 var47_line371
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def var48_line375 nil)


(def
 var49_line381
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def var50_line385 nil)


(def
 var51_line391
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def var52_line397 nil)


(def
 var53_line407
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


(def var54_line415 nil)


(def
 var55_line420
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def var56_line424 nil)


(def
 var57_line426
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


(def var58_line439 nil)


(def
 var59_line441
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example", :favicon "notebooks/favicon.ico"},
    :clean-up-target-dir true})))


(def var60_line453 nil)


(def
 var61_line455
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


(def var62_line468 nil)


(def var63_line471 (comment (clay/browse!)))


(def var64_line474 nil)


(def
 var65_line492
 (comment (clay/make-hiccup {:source-path "notebooks/index.clj"})))


(def var66_line495 nil)


(def
 var67_line558
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


(def var68_line566 (->> (range 3) kind/fragment))


(def var69_line569 nil)


(def
 var70_line578
 (kind/fn {:x 1, :y 2} {:kindly/f (fn [{:keys [x y]}] (+ x y))}))


(def
 var71_line583
 (kind/fn
  {:my-video-src
   "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  {:kindly/f
   (fn [{:keys [my-video-src]}] (kind/video {:src my-video-src}))}))


(def var72_line588 nil)


(def var73_line590 (kind/fn [+ 1 2]))


(def var74_line593 nil)


(def
 var75_line595
 (kind/fn {:kindly/f (fn [{:keys [x y]}] (+ x y)), :x 1, :y 2}))


(def var76_line601 nil)


(def
 var77_line604
 (kind/fn
  {:x (range 3), :y (repeatedly 3 rand)}
  {:kindly/f tc/dataset}))


(def
 var78_line609
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def
 var79_line614
 (kind/fn {:kindly/f tc/dataset, :x (range 3), :y (repeatedly 3 rand)}))


(def var80_line619 nil)


(def var81_line627 (delay (Thread/sleep 500) (+ 1 2)))


(def var82_line631 nil)


(def
 var83_line637
 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def var84_line640 (kind/image {:src "notebooks/images/Clay.svg.png"}))


(def
 var85_line643
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


(def var86_line655 nil)


(def var87_line668 (+ 1 2))


(deftest test88_line670 (is (> var87_line668 2.9)))


(deftest test89_line673 (is (> var87_line668 2.9)))


(deftest test90_line675 (is (> var87_line668 2.9)))


(def var91_line677 nil)


(def
 var92_line684
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def var93_line693 nil)


(def
 var94_line703
 (kind/table
  {:column-names ["A" "B" "C"], :row-vectors [[1 2 3] [4 5 6]]}
  {:class "table-responsive", :style {:background "#f8fff8"}}))


(def var95_line708 nil)


(def
 var96_line739
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :horizontal})
  false))


(def var97_line743 (+ 1 2))


(def var98_line745 (+ 3 4))


(def var99_line747 nil)


(def
 var100_line749
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :vertical})
  false))


(def var101_line753 (+ 1 2))


(def var102_line755 (+ 3 4))


(def var103_line757 nil)


(def
 var104_line759
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color "#ccddee"}})
  false))


(def var105_line763 (kind/hiccup [:div [:p "hello"]]))


(def var106_line767 nil)


(def var107_line770 (tc/dataset {:x (range 3)}))


(def var108_line772 nil)


(def var109_line774 (kind/hiccup [:div (tc/dataset {:x (range 3)})]))


(def var110_line778 nil)


(def
 var111_line780
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color nil}})
  false))


(def var112_line784 (kind/hiccup [:div [:p "hello"]]))
