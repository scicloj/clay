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
 var3_line40
 ["(require '[scicloj.clay.v2.api :as clay])\n(clay/make! {:source-path \"notebooks/index.clj\"})"])


(def var4_line42 nil)


(def
 var5_line64
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


(def var6_line92 nil)


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
  [scicloj.tableplot.v1.hanami :as hanami]
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
 var31_line247
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :live-reload true})))


(def var32_line253 nil)


(def
 var33_line257
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :single-form '(+ 1 2)})))


(def var34_line261 nil)


(def var35_line265 (comment (clay/make! {:single-form '(+ 1 2)})))


(def var36_line268 nil)


(def var37_line271 (comment (clay/make! {:single-value 3})))


(def var38_line274 nil)


(def
 var39_line278
 (comment
  (clay/make!
   {:single-value 3333,
    :post-process (fn [html] (-> html (str/replace #"3333" "4444")))})))


(def var40_line284 nil)


(def
 var41_line287
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-ui-header true})))


(def var42_line291 nil)


(def
 var43_line294
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-info-line true})))


(def var44_line298 nil)


(def
 var45_line303
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def var46_line307 nil)


(def
 var47_line314
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def var48_line319 nil)


(def
 var49_line325
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def var50_line329 nil)


(def
 var51_line335
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def var52_line339 nil)


(def
 var53_line345
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def var54_line351 nil)


(def
 var55_line361
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


(def var56_line369 nil)


(def
 var57_line374
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def var58_line378 nil)


(def
 var59_line380
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


(def var60_line393 nil)


(def
 var61_line395
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example", :favicon "notebooks/favicon.ico"},
    :clean-up-target-dir true})))


(def var62_line407 nil)


(def
 var63_line409
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


(def var64_line422 nil)


(def var65_line425 (comment (clay/browse!)))


(def var66_line428 nil)


(def
 var67_line442
 (comment (clay/make-hiccup {:source-path "notebooks/index.clj"})))


(def var68_line445 nil)


(def
 var69_line508
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


(def var70_line516 (->> (range 3) kind/fragment))


(def var71_line519 nil)


(def
 var72_line528
 (kind/fn {:x 1, :y 2} #:kindly{:f (fn [{:keys [x y]}] (+ x y))}))


(def
 var73_line533
 (kind/fn
  {:my-video-src
   "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  #:kindly{:f
           (fn
            [{:keys [my-video-src]}]
            (kind/video {:src my-video-src}))}))


(def var74_line538 nil)


(def var75_line540 (kind/fn [+ 1 2]))


(def var76_line543 nil)


(def
 var77_line545
 (kind/fn {:kindly/f (fn [{:keys [x y]}] (+ x y)), :x 1, :y 2}))


(def var78_line551 nil)


(def
 var79_line554
 (kind/fn
  {:x (range 3), :y (repeatedly 3 rand)}
  #:kindly{:f tc/dataset}))


(def
 var80_line559
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def
 var81_line564
 (kind/fn {:kindly/f tc/dataset, :x (range 3), :y (repeatedly 3 rand)}))


(def var82_line569 nil)


(def var83_line577 (delay (Thread/sleep 500) (+ 1 2)))


(def var84_line581 nil)


(def
 var85_line587
 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def var86_line590 (kind/image {:src "notebooks/images/Clay.svg.png"}))


(def
 var87_line593
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


(def var88_line605 nil)


(def var89_line618 (+ 1 2))


(deftest test90_line620 (is (> var89_line618 2.9)))


(deftest test91_line623 (is (> var89_line618 2.9)))


(deftest test92_line625 (is (> var89_line618 2.9)))


(def var93_line627 nil)


(def
 var94_line634
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def var95_line643 nil)


(def
 var96_line653
 (kind/table
  {:column-names ["A" "B" "C"], :row-vectors [[1 2 3] [4 5 6]]}
  {:class "table-responsive", :style {:background "#f8fff8"}}))


(def var97_line658 nil)


(def
 var98_line689
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :horizontal})
  false))


(def var99_line693 (+ 1 2))


(def var100_line695 (+ 3 4))


(def var101_line697 nil)


(def
 var102_line699
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :vertical})
  false))


(def var103_line703 (+ 1 2))


(def var104_line705 (+ 3 4))


(def var105_line707 nil)


(def
 var106_line709
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color "#ccddee"}})
  false))


(def var107_line713 (kind/hiccup [:div [:p "hello"]]))


(def var108_line717 nil)


(def var109_line720 (tc/dataset {:x (range 3)}))


(def var110_line722 nil)


(def var111_line724 (kind/hiccup [:div (tc/dataset {:x (range 3)})]))


(def var112_line728 nil)


(def
 var113_line730
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color nil}})
  false))


(def var114_line734 (kind/hiccup [:div [:p "hello"]]))
