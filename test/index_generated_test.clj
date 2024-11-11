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
 var3_line92
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


(def var4_line120 nil)


(def
 var5_line166
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:single-form '~form-before-caret\n                 :source-path [\"~file-path\"]}))"])


(def var6_line169 nil)


(def
 var7_line174
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:source-path [\"~file-path\"]}))"])


(def var8_line176 nil)


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


(def var10_line199 nil)


(def
 var11_line200
 (kind/hiccup
  [:div
   {:style {:background "#efe9e6", :border-style :solid}}
   [:ul [:li "one"] [:li "two"] [:li "three"]]]))


(def var12_line208 nil)


(def
 var13_line209
 (->
  {:x (range 5), :y (repeatedly 5 rand)}
  tc/dataset
  (tc/set-dataset-name "my dataset")))


(def var14_line214 nil)


(def
 var15_line215
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


(def var16_line226 nil)


(def var17_line228 (require '[scicloj.clay.v2.api :as clay]))


(def var18_line230 nil)


(def
 var19_line237
 (comment
  (clay/make! {:format [:html], :source-path "notebooks/index.clj"})))


(def var20_line241 nil)


(def
 var21_line243
 (comment (clay/make! {:source-path "notebooks/index.clj"})))


(def var22_line246 nil)


(def
 var23_line250
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :show false})))


(def var24_line254 nil)


(def
 var25_line257
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj",
    :favicon "notebooks/favicon.ico"})))


(def var26_line261 nil)


(def
 var27_line265
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :show false})))


(def var28_line270 nil)


(def
 var29_line275
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :live-reload true})))


(def var30_line281 nil)


(def
 var31_line285
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :single-form '(+ 1 2)})))


(def var32_line289 nil)


(def var33_line293 (comment (clay/make! {:single-form '(+ 1 2)})))


(def var34_line296 nil)


(def var35_line299 (comment (clay/make! {:single-value 3})))


(def var36_line302 nil)


(def
 var37_line306
 (comment
  (clay/make!
   {:single-value 3333,
    :post-process (fn [html] (-> html (str/replace #"3333" "4444")))})))


(def var38_line312 nil)


(def
 var39_line315
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-ui-header true})))


(def var40_line319 nil)


(def
 var41_line322
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-info-line true})))


(def var42_line326 nil)


(def
 var43_line331
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def var44_line335 nil)


(def
 var45_line342
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def var46_line347 nil)


(def
 var47_line353
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def var48_line357 nil)


(def
 var49_line363
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def var50_line367 nil)


(def
 var51_line373
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def var52_line379 nil)


(def
 var53_line389
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


(def var54_line397 nil)


(def
 var55_line402
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def var56_line406 nil)


(def
 var57_line408
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


(def var58_line421 nil)


(def
 var59_line423
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example", :favicon "notebooks/favicon.ico"},
    :clean-up-target-dir true})))


(def var60_line435 nil)


(def
 var61_line437
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


(def var62_line450 nil)


(def var63_line453 (comment (clay/browse!)))


(def var64_line456 nil)


(def
 var65_line474
 (comment (clay/make-hiccup {:source-path "notebooks/index.clj"})))


(def var66_line477 nil)


(def
 var67_line540
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


(def var68_line548 (->> (range 3) kind/fragment))


(def var69_line551 nil)


(def
 var70_line560
 (kind/fn {:x 1, :y 2} #:kindly{:f (fn [{:keys [x y]}] (+ x y))}))


(def
 var71_line565
 (kind/fn
  {:my-video-src
   "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  #:kindly{:f
           (fn
            [{:keys [my-video-src]}]
            (kind/video {:src my-video-src}))}))


(def var72_line570 nil)


(def var73_line572 (kind/fn [+ 1 2]))


(def var74_line575 nil)


(def
 var75_line577
 (kind/fn {:kindly/f (fn [{:keys [x y]}] (+ x y)), :x 1, :y 2}))


(def var76_line583 nil)


(def
 var77_line586
 (kind/fn
  {:x (range 3), :y (repeatedly 3 rand)}
  #:kindly{:f tc/dataset}))


(def
 var78_line591
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def
 var79_line596
 (kind/fn {:kindly/f tc/dataset, :x (range 3), :y (repeatedly 3 rand)}))


(def var80_line601 nil)


(def var81_line609 (delay (Thread/sleep 500) (+ 1 2)))


(def var82_line613 nil)


(def
 var83_line619
 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def var84_line622 (kind/image {:src "notebooks/images/Clay.svg.png"}))


(def
 var85_line625
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


(def var86_line637 nil)


(def var87_line650 (+ 1 2))


(deftest test88_line652 (is (> var87_line650 2.9)))


(deftest test89_line655 (is (> var87_line650 2.9)))


(deftest test90_line657 (is (> var87_line650 2.9)))


(def var91_line659 nil)


(def
 var92_line666
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def var93_line675 nil)


(def
 var94_line685
 (kind/table
  {:column-names ["A" "B" "C"], :row-vectors [[1 2 3] [4 5 6]]}
  {:class "table-responsive", :style {:background "#f8fff8"}}))


(def var95_line690 nil)


(def
 var96_line721
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :horizontal})
  false))


(def var97_line725 (+ 1 2))


(def var98_line727 (+ 3 4))


(def var99_line729 nil)


(def
 var100_line731
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :vertical})
  false))


(def var101_line735 (+ 1 2))


(def var102_line737 (+ 3 4))


(def var103_line739 nil)


(def
 var104_line741
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color "#ccddee"}})
  false))


(def var105_line745 (kind/hiccup [:div [:p "hello"]]))


(def var106_line749 nil)


(def var107_line752 (tc/dataset {:x (range 3)}))


(def var108_line754 nil)


(def var109_line756 (kind/hiccup [:div (tc/dataset {:x (range 3)})]))


(def var110_line760 nil)


(def
 var111_line762
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color nil}})
  false))


(def var112_line766 (kind/hiccup [:div [:p "hello"]]))
