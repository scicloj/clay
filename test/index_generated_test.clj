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
 var19_line256
 (comment
  (clay/make! {:format [:html], :source-path "notebooks/index.clj"})))


(def var20_line260 nil)


(def
 var21_line262
 (comment (clay/make! {:source-path "notebooks/index.clj"})))


(def var22_line265 nil)


(def
 var23_line270
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :browse false})))


(def var24_line274 nil)


(def
 var25_line278
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :show false})))


(def var26_line282 nil)


(def
 var27_line285
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj",
    :favicon "notebooks/favicon.ico"})))


(def var28_line289 nil)


(def
 var29_line293
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :show false})))


(def var30_line298 nil)


(def
 var31_line303
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :live-reload true})))


(def var32_line309 nil)


(def
 var33_line313
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :single-form '(+ 1 2)})))


(def var34_line317 nil)


(def var35_line321 (comment (clay/make! {:single-form '(+ 1 2)})))


(def var36_line324 nil)


(def var37_line327 (comment (clay/make! {:single-value 3})))


(def var38_line330 nil)


(def
 var39_line334
 (comment
  (clay/make!
   {:single-value 3333,
    :post-process (fn [html] (-> html (str/replace #"3333" "4444")))})))


(def var40_line340 nil)


(def
 var41_line343
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-ui-header true})))


(def var42_line347 nil)


(def
 var43_line350
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-info-line true})))


(def var44_line354 nil)


(def
 var45_line359
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def var46_line363 nil)


(def
 var47_line370
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def var48_line375 nil)


(def
 var49_line381
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def var50_line385 nil)


(def
 var51_line391
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def var52_line395 nil)


(def
 var53_line401
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def var54_line407 nil)


(def
 var55_line417
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


(def var56_line425 nil)


(def
 var57_line430
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def var58_line434 nil)


(def
 var59_line436
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


(def var60_line449 nil)


(def
 var61_line451
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example", :favicon "notebooks/favicon.ico"},
    :clean-up-target-dir true})))


(def var62_line463 nil)


(def
 var63_line465
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


(def var64_line478 nil)


(def var65_line481 (comment (clay/browse!)))


(def var66_line484 nil)


(def
 var67_line502
 (comment (clay/make-hiccup {:source-path "notebooks/index.clj"})))


(def var68_line505 nil)


(def
 var69_line569
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


(def var70_line577 (->> (range 3) kind/fragment))


(def var71_line580 nil)


(def
 var72_line589
 (kind/fn {:x 1, :y 2} #:kindly{:f (fn [{:keys [x y]}] (+ x y))}))


(def
 var73_line594
 (kind/fn
  {:my-video-src
   "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  #:kindly{:f
           (fn
            [{:keys [my-video-src]}]
            (kind/video {:src my-video-src}))}))


(def var74_line599 nil)


(def var75_line601 (kind/fn [+ 1 2]))


(def var76_line604 nil)


(def
 var77_line606
 (kind/fn {:kindly/f (fn [{:keys [x y]}] (+ x y)), :x 1, :y 2}))


(def var78_line612 nil)


(def
 var79_line615
 (kind/fn
  {:x (range 3), :y (repeatedly 3 rand)}
  #:kindly{:f tc/dataset}))


(def
 var80_line620
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def
 var81_line625
 (kind/fn {:kindly/f tc/dataset, :x (range 3), :y (repeatedly 3 rand)}))


(def var82_line630 nil)


(def var83_line638 (delay (Thread/sleep 500) (+ 1 2)))


(def var84_line642 nil)


(def
 var85_line648
 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def var86_line651 (kind/image {:src "notebooks/images/Clay.svg.png"}))


(def
 var87_line654
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


(def var88_line666 nil)


(def var89_line679 (+ 1 2))


(deftest test90_line681 (is (> var89_line679 2.9)))


(deftest test91_line684 (is (> var89_line679 2.9)))


(deftest test92_line686 (is (> var89_line679 2.9)))


(def var93_line688 nil)


(def
 var94_line695
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def var95_line704 nil)


(def
 var96_line714
 (kind/table
  {:column-names ["A" "B" "C"], :row-vectors [[1 2 3] [4 5 6]]}
  {:class "table-responsive", :style {:background "#f8fff8"}}))


(def var97_line719 nil)


(def
 var98_line750
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :horizontal})
  false))


(def var99_line754 (+ 1 2))


(def var100_line756 (+ 3 4))


(def var101_line758 nil)


(def
 var102_line760
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :vertical})
  false))


(def var103_line764 (+ 1 2))


(def var104_line766 (+ 3 4))


(def var105_line768 nil)


(def
 var106_line770
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color "#ccddee"}})
  false))


(def var107_line774 (kind/hiccup [:div [:p "hello"]]))


(def var108_line778 nil)


(def var109_line781 (tc/dataset {:x (range 3)}))


(def var110_line783 nil)


(def var111_line785 (kind/hiccup [:div (tc/dataset {:x (range 3)})]))


(def var112_line789 nil)


(def
 var113_line791
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color nil}})
  false))


(def var114_line795 (kind/hiccup [:div [:p "hello"]]))
