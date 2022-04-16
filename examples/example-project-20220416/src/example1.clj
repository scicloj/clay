;; # Clay

;; ![quaternary clay in Estonia](https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg)
;; (credit: [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:Clay-ss-2005.jpg))

;; ## What is it?

;; [Clay](https://github.com/scicloj/clay) is a tiny Clojure tool offering a dynamic workflow using some of the more serious visual tools & libraries such as [Portal](https://github.com/djblue/portal), [Clerk](https://github.com/nextjournal/clerk), and [Scittle](https://github.com/babashka/scittle). Here, by visual tools we mean tools for data visualization and literate programming.

;; It is one of the fruits of our explorations at the [visual-tools-group](https://scicloj.github.io/docs/community/groups/visual-tools/).

;; ## The problem

;; Clojure has [an amazing diversity](https://scicloj.github.io/docs/resources/libs/#visual-tools-literate-programming-and-data-visualization) of tools for data visualization and literate programming.

;; Each tool has its own conventions of specifying how things should be shown visually.

;; - [Anglican tutorials](https://probprog.github.io/anglican/examples/index.html) ([source](https://bitbucket.org/probprog/anglican-examples/src/master/worksheets/)) - written in [Gorilla REPL](https://github.com/JonyEpsilon/gorilla-repl)
;; - [thi-ng/geom viz examples](https://github.com/thi-ng/geom/blob/feature/no-org/org/examples/viz/demos.org)  ([source](https://raw.githubusercontent.com/thi-ng/geom/feature/no-org/org/examples/viz/demos.org)) - written in [Org-babel-clojure](https://orgmode.org/worg/org-contrib/babel/languages/ob-doc-clojure.html)
;; - [Clojure2d docs](https://github.com/Clojure2D/clojure2d#Usage) ([source1](https://github.com/Clojure2D/clojure2d/blob/master/src/clojure2d), [source2](https://github.com/Clojure2D/clojure2d/blob/master/metadoc/clojure2d/)) - written in [Codox](https://github.com/weavejester/codox) and [Metadoc](https://github.com/generateme/metadoc)
;; - [Tablecloth API docs](https://scicloj.github.io/tablecloth/index.html) ([source](https://github.com/scicloj/tablecloth/blob/master/docs/index.Rmd)) - written in [rmarkdown-clojure](https://github.com/genmeblog/rmarkdown-clojure)
;; - [R interop ClojisR examples](https://github.com/scicloj/clojisr-examples) ([source](https://github.com/scicloj/clojisr-examples/tree/master/src/clojisr_examples)) - written in [Notespace v2](https://github.com/scicloj/notespace/blob/master/doc/v2.md)
;; - [Bayesian optimization tutorial](https://nextjournal.com/generateme/bayesian-optimization) ([source](https://nextjournal.com/generateme/bayesian-optimization)) - written in [Nextjournal](https://nextjournal.com/)
;; - [scicloj.ml tutorials](https://github.com/scicloj/scicloj.ml-tutorials#tutorials-for-sciclojml) ([source](https://github.com/scicloj/scicloj.ml-tutorials/tree/main/src/scicloj/ml)) - written in [Notespace v3](https://github.com/scicloj/notespace/blob/master/doc/v3.md)
;; - [Clojure2d color tutorial](https://clojure2d.github.io/clojure2d/docs/notebooks/index.html#/notebooks/color.clj) ([source](https://github.com/Clojure2D/clojure2d/blob/master/notebooks/color.clj)) - written in [Clerk](https://github.com/nextjournal/)


;;[tweet](https://twitter.com/quoll/status/1513339079974428674)

;; ### Goals

;; * *Easily* **explore & share** things for others to *easily* **pick & use**.

;; * In **common** use cases, have **compatible** experiences at the relevant tools.

;; * In **all** use cases, be able to use **the best** tools.

;; * Grow naturally with future tools.

;; * Flow with the REPL.

;; ### The stack

;; Clay is part of a stack of libraries seeking easy experience with common data-centric tasks.
;; - [Kindly](https://github.com/scicloj/kindly) - a tiny library  for specifying the kind of way different things should be viewed
;; - [Clay](https://github.com/scicloj/clay) - a dynamic workflow for visual exploration & documentation, combining different tools using Kindly
;; - [Viz.clj](https://github.com/scicloj/viz.clj) - a (work-in-progress) library for easy data visualizations, which is Kindly-aware, and thus fits nicely with Clay

;; ## Setup

(ns example1
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.clay.v1.tools :as tools]
            [scicloj.clay.v1.tool.scittle :as tool.scittle]
            [scicloj.clay.v1.html.table :as table]
            [tech.v3.dataset :as tmd]
            [tablecloth.api :as tc]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind]
            [scicloj.kindly.v2.kindness :as kindness]
            [scicloj.viz.api :as viz]
            [scicloj.clay.v1.util.image :as util.image]
            [tech.v3.tensor :as tensor]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as fun]
            [tech.v3.libs.buffered-image :as bufimg]
            [clojure2d.color :as color]))

(kindly/define-kind-behaviour!
  :kind/dataset
  {:scittle.viewer (fn [v]
                     ['datatables
                      (-> {:column-names (vec (tmd/column-names v))
                           :row-vectors (vec (tmd/rowvecs v))}
                          table/->table-hiccup)])})

(clay/start! {:tools [tools/clerk
                      tools/portal
                      tools/scittle]})

(comment
  (clay/restart! {:tools [tools/clerk
                          tools/portal
                          tools/scittle]}))


(+ 1 9)


;; ## Kinds

;; The naive kind just behaves as usual, in any tool.

(-> {:x 9}
    kind/naive)

;; ### Hiccup

(-> [:p {:style ; https://www.htmlcsscolor.com/hex/7F5F3F
         {:color "#7F5F3F"}}
     "hello"]
    kind/hiccup)

;; ### Images

(import java.awt.image.BufferedImage
        java.awt.Color
        sun.java2d.SunGraphics2D)

(defn a-piece-of-random-art [n]
  (let [bi (BufferedImage. n n BufferedImage/TYPE_INT_RGB)
        g  (-> (.createGraphics ^BufferedImage bi))]
    (dotimes [t 100]
      (->> #(rand-int n)
           (repeatedly 4)
           (apply #(.drawLine ^SunGraphics2D g %1 %2 %3 %4))))
    bi))

(repeatedly 4 #(a-piece-of-random-art (inc (rand-int 100))))

;; ### Tables

(-> {:column-names [:preferred-language :age]
     :row-vectors (for [i (range 99)]
                    [(["clojure" "clojurescript" "babashka"]
                      (rand-int 3))
                     (rand-int 100)])}
    (kindly/consider kind/table))

(-> {:column-names [:preferred-language :age]
     :row-maps (for [i (range 99)]
                 {:preferred-language (["clojure" "clojurescript" "babashka"]
                                       (rand-int 3))
                  :age (rand-int 100)})}
    (kindly/consider kind/table))

;; ### [Vega](https://vega.github.io/vega/) and [Vega-Lite](https://vega.github.io/vega-lite/)

(def vega-lite-spec
  (memoize
   (fn [n]
     (-> {:data {:values
                 (->> (repeatedly n #(- (rand) 0.5))
                      (reductions +)
                      (map-indexed (fn [x y]
                                     {:w (rand-int 9)
                                      :z (rand-int 9)
                                      :x x
                                      :y y})))},
          :mark "point"
          :encoding
          {:size {:field "w" :type "quantitative"}
           :x {:field "x", :type "quantitative"},
           :y {:field "y", :type "quantitative"},
           :fill {:field "z", :type "nominal"}}}
         (kindly/consider kind/vega)))))

(vega-lite-spec 9)

;; ### [Cytoscape.js](https://js.cytoscape.org/)

(kind/cytoscape
 {:elements {:nodes [{:data {:id "a" :parent "b"} :position {:x 215 :y 85}}
                     {:data {:id "b"}}
                     {:data {:id "c" :parent "b"} :position {:x 300 :y 85}}
                     {:data {:id "d"} :position {:x 215 :y 175}}
                     {:data {:id "e"}}
                     {:data {:id "f" :parent "e"} :position {:x 300 :y 175}}]
             :edges [{:data {:id "ad" :source "a" :target "d"}}
                     {:data {:id "eb" :source "e" :target "b"}}]}
  :style [{:selector "node"
           :css {:content "data(id)"
                 :text-valign "center"
                 :text-halign "center"}}
          {:selector "parent"
           :css {:text-valign "top"
                 :text-halign "center"}}
          {:selector "edge"
           :css {:curve-style "bezier"
                 :target-arrow-shape "triangle"}}]
  :layout {:name "preset"
           :padding 5}})

;; ### [Apache Echarts](https://echarts.apache.org/)

(kind/echarts
 {:xAxis {:data ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]}
  :yAxis {}
  :series [{:type "bar"
            :color ["#7F5F3F"]
            :data [23 24 18 25 27 28 25]}]})

;; ## Delays

(delay
  (Thread/sleep 500)
  (+ 1 2))

(delay
  (-> [:div [:big "hi......."]]
      (kindly/consider kind/hiccup)))

;; ## Tests

(-> 2
    (+ 3)
    (clay/check = 4))

(-> 2
    (+ 3)
    (clay/check = 5))

;; ## Nesting

(-> (->> [10 100 1000]
         (map (fn [n]
                [:div {:style {:width "400px"}}
                 [:big (str "n=" n)]
                 (vega-lite-spec n)]))
         (into [:div]))
    (kindly/consider kind/hiccup))


;; ## Viz.clj

;; ### datasets

(-> (let [n 99]
      (tc/dataset {:preferred-language (for [i (range n)]
                                         (["clojure" "clojurescript" "babashka"]
                                          (rand-int 3)))
                   :age (for [i (range n)]
                          (rand-int 100))})))

;; ### Vega-Lite

(-> [{:x 1 :y 2}
     {:x 2 :y 4}
     {:x 3 :y 9}]
    viz/data
    (viz/type :point)
    (viz/mark-size 200)
    (viz/color :x)
    #_(assoc :BACKGROUND "#eeeee0")
    viz/viz
    #_(assoc :height 100))

;; ## Widgets with Scittle

(let [data {:day ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]
            :rain [23 24 18 25 27 28 25]}]
  (tool.scittle/show-widget!
   '[(fn []
       (let [*state (r/atom {})]
         (fn []
           [:div
            [echarts
             {:xAxis {:data (:day data)}
              :yAxis {}
              :series [{:type "bar"
                        :data (:rain data)}]}
             {:on {:click #(do
                             (println (-> %  js->clj pr-str))
                             (compute [:fn1
                                       (-> % .-dataIndex)]
                                      *state [:day]))}}]
            [:hr]
            [:h1 (:day @*state)]])))]
   {:data data
    :fns {:fn1 (fn [i]
                 (-> data
                     :day
                     (get i)))}}))




;; ## Near future

;; - project templates

;; - various target outputs

;; - community exploration

;; - DSL exploration


;; ## Discussion

;; - [visual-tools-group](https://scicloj.github.io/docs/community/groups/visual-tools/)

;; - getting involved


;; ## Exploration

(def image1
  (-> "https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg"
      #_"https://upload.wikimedia.org/wikipedia/commons/thumb/8/85/WMEE-exp2019-%28113%29.jpg/2880px-WMEE-exp2019-%28113%29.jpg"
      #_"https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/Clojure_logo.svg/1920px-Clojure_logo.svg.png"
      util.image/load-buffered-image))


(defn region-mean [image-tensor y x radius]
  (let [[height width color-components] (dtype/shape image-tensor)]
    (fun// (-> (tensor/compute-tensor [height width color-components]
                                      (fn [y1 x1 c]
                                        (if (< (+ (-> (- x1 x) (#(* % %)))
                                                  (-> (- y1 y) (#(* % %))))
                                               (* radius radius))
                                          (image-tensor y1 x1 c)
                                          0))
                                      :int32)
               dtype/clone
               (#(tensor/reduce-axis fun/sum % 0))
               (#(tensor/reduce-axis fun/sum % 0)))
           (-> (tensor/compute-tensor [height width color-components]
                                      (fn [y1 x1 c]
                                        ;; (println (+ (-> (- x1 x) (#(* % %)))
                                        ;;             (-> (- y1 y) (#(* % %)))))
                                        (if (< (+ (-> (- x1 x) (#(* % %)))
                                                  (-> (- y1 y) (#(* % %))))
                                               (* radius radius))
                                          1
                                          0))
                                      :int32)
               dtype/clone
               (#(tensor/reduce-axis fun/sum % 0))
               (#(tensor/reduce-axis fun/sum % 0))))))


(-> image1
    tensor/ensure-tensor)

(-> image1
    tensor/ensure-tensor
    (region-mean 200 200 20))

(defn tensor->img [t]
  (let [shape (dtype/shape t)
        new-img (bufimg/new-image (shape 0)
                                  (shape 1)
                                  :byte-bgr)]
    (dtype/copy! t
                 (tensor/ensure-tensor new-img))
    new-img))


(-> image1
    tensor/ensure-tensor
    (tensor/select (range 200)
                   (range 200)
                   :all)
    tensor->img)


(let [tensor1 (-> image1 tensor/ensure-tensor)
      [height width] (dtype/shape tensor1)]
  (tool.scittle/show-widget!
   '[(fn []
       (let [*state (r/atom {})]
         (fn []
           [:div
            [echarts
             {:graphic [{:type :image
                         :style {:image (:image data)}}]}
             {:on {:click #(compute [:colors
                                     (-> % .-event .-offsetX)
                                     (-> % .-event .-offsetY)]
                                    *state [:colors])}}]
            [:hr]
            (when-let [{:keys [colors]} @*state]
              [:div
               [:p (pr-str colors)]
               [:b [:big "point"]]
               [:div {:style {:width (:width data)
                              :background (:point colors)}}
                [:h1 (:point colors)]]
               [:b [:big "region"]]
               [:div {:style {:width (:width data)
                              :background (:region colors)}}
                [:h1 (:region colors)]]])])))]
   {:data {:image (-> image1
                      util.image/buffered-image->byte-array
                      util.image/byte-array->data-uri)
           :width width
           :heigh height}
    :fns {:colors (fn [x y]
                    {:x x
                     :y y
                     :point-dbg  (-> (tensor1 y x)
                                     vec)
                     :point (->> (tensor1 y x)
                                 reverse
                                 (apply color/color)
                                 color/format-hex)
                     :region (->> (region-mean tensor1 y x 20)
                                  reverse
                                  (apply color/color)
                                  color/format-hex)})}}))
