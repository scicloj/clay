(def
 var1_line5
 [:img
  {:style {:width "100px"},
   :src
   "https://raw.githubusercontent.com/scicloj/clay/main/resources/Clay.svg.png",
   :alt "Clay logo"}])


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
    "GsML75MtNXw"]
   ["Jan. 24th 2025"
    "Noj v2 - getting started - from raw data to a blog post (demonstrating CIDER integration and Quarto publishing)"
    "vnvcKtHHMVQ"]]
  reverse
  (map
   (fn
    [[date title youtube-id]]
    [:tr [:td date] [:td title] [:td {:youtube-id youtube-id}]]))
  (into [:table])))


(def
 var5_line186
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:single-form '~form-before-caret\n                 :source-path [\"~file-path\"]}))"])


(def
 var7_line194
 ["(do (require '[scicloj.clay.v2.api :as clay])\n    (clay/make! {:source-path [\"~file-path\"]}))"])


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


(def
 var11_line220
 (kind/hiccup
  [:div
   {:style {:background "#efe9e6", :border-style :solid}}
   [:ul [:li "one"] [:li "two"] [:li "three"]]]))


(def
 var13_line229
 (->
  {:x (range 5), :y (repeatedly 5 rand)}
  tc/dataset
  (tc/set-dataset-name "my dataset")))


(def
 var15_line235
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


(def var17_line248 (require '[scicloj.clay.v2.api :as clay]))


(def
 var19_line259
 (comment
  (clay/make! {:format [:html], :source-path "notebooks/index.clj"})))


(def
 var21_line265
 (comment (clay/make! {:source-path "notebooks/index.clj"})))


(def
 var23_line273
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :browse false})))


(def
 var25_line281
 (comment
  (clay/make! {:source-path "notebooks/index.clj", :show false})))


(def
 var27_line288
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj",
    :favicon "notebooks/favicon.ico"})))


(def
 var29_line296
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :show false})))


(def
 var31_line306
 (comment
  (clay/make!
   {:source-path ["notebooks/slides.clj" "notebooks/index.clj"],
    :live-reload true})))


(def
 var33_line316
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :single-form '(+ 1 2)})))


(def var35_line324 (comment (clay/make! {:single-form '(+ 1 2)})))


(def var37_line330 (comment (clay/make! {:single-value 3})))


(def
 var39_line337
 (comment
  (clay/make!
   {:single-value 3333,
    :post-process (fn [html] (-> html (str/replace #"3333" "4444")))})))


(def
 var41_line346
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-ui-header true})))


(def
 var43_line353
 (comment
  (clay/make!
   {:source-path "notebooks/index.clj", :hide-info-line true})))


(def
 var45_line362
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/index.clj"})))


(def
 var47_line373
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :run-quarto false})))


(def
 var49_line384
 (comment
  (clay/make!
   {:format [:quarto :html], :source-path "notebooks/slides.clj"})))


(def
 var51_line394
 (comment
  (clay/make!
   {:format [:quarto :revealjs], :source-path "notebooks/slides.clj"})))


(def
 var53_line404
 (comment
  (clay/make!
   {:format [:quarto :html],
    :source-path "notebooks/index.clj",
    :quarto
    {:highlight-style :nord, :format {:html {:theme :journal}}}})))


(def
 var55_line420
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
 var57_line433
 (comment
  (clay/make!
   {:base-source-path "notebooks/", :source-path "index.clj"})))


(def
 var59_line439
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
 var61_line454
 (comment
  (clay/make!
   {:format [:quarto :html],
    :base-source-path "notebooks",
    :source-path ["index.clj" "chapter.clj" "another_chapter.md"],
    :base-target-path "book",
    :book {:title "Book Example", :favicon "notebooks/favicon.ico"},
    :clean-up-target-dir true})))


(def
 var63_line468
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


(def var65_line484 (comment (clay/browse!)))


(def
 var67_line505
 (comment (clay/make-hiccup {:source-path "notebooks/index.clj"})))


(def
 var69_line572
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


(def var70_line580 (->> (range 3) kind/fragment))


(def
 var72_line592
 (kind/fn {:x 1, :y 2} #:kindly{:f (fn [{:keys [x y]}] (+ x y))}))


(def
 var73_line597
 (kind/fn
  {:my-video-src
   "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  #:kindly{:f
           (fn
            [{:keys [my-video-src]}]
            (kind/video {:src my-video-src}))}))


(def var75_line604 (kind/fn [+ 1 2]))


(def
 var77_line609
 (kind/fn {:kindly/f (fn [{:keys [x y]}] (+ x y)), :x 1, :y 2}))


(def
 var79_line618
 (kind/fn
  {:x (range 3), :y (repeatedly 3 rand)}
  #:kindly{:f tc/dataset}))


(def
 var80_line623
 (kind/fn [tc/dataset {:x (range 3), :y (repeatedly 3 rand)}]))


(def
 var81_line628
 (kind/fn {:kindly/f tc/dataset, :x (range 3), :y (repeatedly 3 rand)}))


(def var83_line641 (delay (Thread/sleep 500) (+ 1 2)))


(def
 var85_line651
 (kind/hiccup [:img {:src "notebooks/images/Clay.svg.png"}]))


(def var86_line654 (kind/image {:src "notebooks/images/Clay.svg.png"}))


(def
 var87_line657
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


(def var89_line682 (+ 1 2))


(deftest test90_line684 (is (> var89_line682 2.9)))


(deftest test91_line687 (is (> var89_line682 2.9)))


(deftest test92_line689 (is (> var89_line682 2.9)))


(def
 var94_line698
 (kindly/hide-code
  (kind/code
   "(kind/test-last [> 2.9])\n\n^kind/test-last\n[> 2.9]\n\n(kindly/check > 2.9)")))


(def
 var96_line717
 (kind/table
  {:column-names ["A" "B" "C"], :row-vectors [[1 2 3] [4 5 6]]}
  {:class "table-responsive", :style {:background "#f8fff8"}}))


(def
 var98_line753
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :horizontal})
  false))


(def var99_line757 (+ 1 2))


(def var100_line759 (+ 3 4))


(def
 var102_line763
 (kindly/hide-code
  (kindly/merge-options! {:code-and-value :vertical})
  false))


(def var103_line767 (+ 1 2))


(def var104_line769 (+ 3 4))


(def
 var106_line773
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color "#ccddee"}})
  false))


(def var107_line777 (kind/hiccup [:div [:p "hello"]]))


(def var109_line784 (tc/dataset {:x (range 3)}))


(def var111_line788 (kind/hiccup [:div (tc/dataset {:x (range 3)})]))


(def
 var113_line794
 (kindly/hide-code
  (kindly/merge-options! {:style {:background-color nil}})
  false))


(def var114_line798 (kind/hiccup [:div [:p "hello"]]))
