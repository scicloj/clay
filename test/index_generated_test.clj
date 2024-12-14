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
 var5_line185
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:single-form '~form-before-caret\n                 :source-path [\"~file-path\"]}))"])


(def var6_line188 nil)


(def
 var7_line193
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:source-path [\"~file-path\"]}))"])


(def var8_line195 nil)


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


(def var10_line218 nil)


(def
 var11_line219
 (kind/hiccup
  [:div
   {:style {:background "#efe9e6", :border-style :solid}}
   [:ul [:li "one"] [:li "two"] [:li "three"]]]))


(def var12_line227 nil)


(def
 var13_line228
 (->
  {:x (range 5), :y (repeatedly 5 rand)}
  tc/dataset
  (tc/set-dataset-name "my dataset")))


(def var14_line233 nil)


(def
 var15_line234
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


(def var16_line245 nil)


(def var17_line247 (require '[scicloj.clay.v2.api :as clay]))


(def var18_line249 nil)


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
 var23_line269
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :show false})))


(def var24_line273 nil)


(def
 var25_line276
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj",
    :favicon "notebooks/favicon.ico"})))


(def var26_line280 nil)


(def
 var27_line284
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :show false})))


(def var28_line289 nil)


(def
 var29_line294
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :live-reload true})))


(def var30_line300 nil)


(def
 var31_line304
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :single-form '(+ 1 2)})))


(def var32_line308 nil)


(def var33_line312 (comment (clay/make! {:single-form '(+ 1 2)})))


(def var34_line315 nil)


(def var35_line318 (comment (clay/make! {:single-value 3})))


(def var36_line321 nil)


(def
 var37_line325
 (comment
  (clay/make!
   {:single-value 3333,
    :post-process (fn [html] (-> html (str/replace #"3333" "4444")))})))


(def var38_line331 nil)


(def
 var39_line334
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-ui-header true})))


(def var40_line338 nil)


(def
 var41_line341
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-info-line true})))


(def var42_line345 nil)


(def
 var43_line350
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def var44_line354 nil)


(def
 var45_line361
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def var46_line366 nil)


(def
 var47_line372
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def var48_line376 nil)


(def
 var49_line382
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def var50_line386 nil)


(def
 var51_line392
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def var52_line398 nil)


(def
 var53_line408
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


(def var54_line416 nil)


(def
 var55_line421
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def var56_line425 nil)


(def
 var57_line427
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


(def var58_line440 nil)


(def
 var59_line442
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example", :favicon "notebooks/favicon.ico"},
    :clean-up-target-dir true})))


(def var60_line454 nil)


(def
 var61_line456
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


(def var62_line469 nil)


(def var63_line472 (comment (clay/browse!)))


(def var64_line475 nil)


(def
 var65_line493
 (comment (clay/make-hiccup {:source-path "notebooks/index.clj"})))


(def var66_line496 nil)


(def
 var67_line559
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


(def var68_line567 (->> (range 3) kind/fragment))


(def var69_line570 nil)


(def
 var70_line579
 (kind/fn {:x 1, :y 2} #:kindly{:f (fn [{:keys [x y]}] (+ x y))}))


(def
 var71_line584
 (kind/fn
  {:my-video-src
   "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  #:kindly{:f
           (fn
            [{:keys [my-video-src]}]
            (kind/video {:src my-video-src}))}))


(def var72_line589 nil)


(def var73_line591 (kind/fn [+ 1 2]))


(def var74_line594 nil)


(def
 var75_line596
 (kind/fn {:kindly/f (fn [{:keys [x y]}] (+ x y)), :x 1, :y 2}))


(def var76_line602 nil)


(def
 var77_line605
 (kind/fn
  {:x (range 3), :y (repeatedly 3 rand)}
  #:kindly{:f tc/dataset}))


(def
 var78_line610
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def
 var79_line615
 (kind/fn {:kindly/f tc/dataset, :x (range 3), :y (repeatedly 3 rand)}))


(def var80_line620 nil)


(def var81_line628 (delay (Thread/sleep 500) (+ 1 2)))


(def var82_line632 nil)


(def
 var83_line638
 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def var84_line641 (kind/image {:src "notebooks/images/Clay.svg.png"}))


(def
 var85_line644
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


(def var86_line656 nil)


(def var87_line669 (+ 1 2))


(deftest test88_line671 (is (> var87_line669 2.9)))


(deftest test89_line674 (is (> var87_line669 2.9)))


(deftest test90_line676 (is (> var87_line669 2.9)))


(def var91_line678 nil)


(def
 var92_line685
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def var93_line694 nil)


(def
 var94_line704
 (kind/table
  {:column-names ["A" "B" "C"], :row-vectors [[1 2 3] [4 5 6]]}
  {:class "table-responsive", :style {:background "#f8fff8"}}))


(def var95_line709 nil)


(def
 var96_line740
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :horizontal})
  false))


(def var97_line744 (+ 1 2))


(def var98_line746 (+ 3 4))


(def var99_line748 nil)


(def
 var100_line750
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :vertical})
  false))


(def var101_line754 (+ 1 2))


(def var102_line756 (+ 3 4))


(def var103_line758 nil)


(def
 var104_line760
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color "#ccddee"}})
  false))


(def var105_line764 (kind/hiccup [:div [:p "hello"]]))


(def var106_line768 nil)


(def var107_line771 (tc/dataset {:x (range 3)}))


(def var108_line773 nil)


(def var109_line775 (kind/hiccup [:div (tc/dataset {:x (range 3)})]))


(def var110_line779 nil)


(def
 var111_line781
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color nil}})
  false))


(def var112_line785 (kind/hiccup [:div [:p "hello"]]))
