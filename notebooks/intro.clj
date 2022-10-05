;; # Clay

^:kindly/hide-code?
(with-meta
  [:img
   {:style {:width "300px"}
    :src "https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg"
    :alt "quaternary clay in Estonia"}]
  {:kindly/kind :kind/hiccup})
;; (credit: [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:Clay-ss-2005.jpg))

;; ## What is it?

;; [Clay](https://github.com/scicloj/clay) is a Clojure tool for data visualization and literate programming,
;; which is compatible with the [Kindly](https://github.com/scicloj/kindly) convention.

;; It is one of the fruits of our explorations at the [visual-tools-group](https://scicloj.github.io/docs/community/groups/visual-tools/).

;; ### Goals

;; * *Easily* **explore & share** things for others to *easily* **pick & use**.

;;   * Small examples, library tutorials, research reports, etc., should be shareable as code that renders to visual documents, that others can use.

;; * In **common** use cases, have **compatible** experiences at the relevant tools.

;;   * Common visualizations such as images, tables, Vega/Vega-Lite plots, plain Clojure data structures, etc., should be describeable in a way that other visual tools can interpret correctly.

;; * **Flow** with the REPL.

;;   * We wish to encourage user interactions that flow naturally with the typical use of Clojure in editors and REPLs, editing and evaluating Clojure forms.

;; ### The stack

;; Clay is part of a stack of libraries seeking easy experience with common data-centric tasks.
;; - [Kindly](https://github.com/scicloj/kindly) - a tiny library for specifying the kind of way things should be viewed
;; - [Clay](https://github.com/scicloj/clay) - a dynamic workflow for visual exploration & documentation
;; - [Viz.clj](https://github.com/scicloj/viz.clj) - a (work-in-progress) library for easy data visualizations on top of [Hanami](https://github.com/jsa-aerial/hanami), which is Kindly-aware, and thus fits nicely with Clay

;; ## Setup

;; For rendering documents like this one with Clay, one only needs to add Clay as a dependency to your project.
;;
;; To enjoy Clay's dynamic interaction, it is also needed to inform it about code evaluations. This requires some setup at the editor.
;;
;; See some suggested setup for popular editors below. If your favourite editor is not supported yet, let us talk and make it work.

;; ### Calva

;; Please add the following command to your keybindings (you may pick another key, of course). This command would evaluate a piece of code and send the result to be visualized in Clay.

;; ```json
;; {
;;  "key": "ctrl+shift+enter",
;;  "command": "calva.runCustomREPLCommand",
;;  "args": "(tap> {:clay-tap? true :form (quote $current-form) :code (str (quote $current-form)) :value $current-form})"
;;  }
;; ```

;; ### CIDER

;; Please add the following to your Emacs configuration. It will make sure to inform Clay about all user evaluations of Clojure code.


;; ```elisp
;; ;; (inspired by: https://github.com/clojure-emacs/cider/issues/3094)
;; (require 'cider-mode)
;;
;; (defun cider-tap (&rest r) ; inspired by https://github.com/clojure-emacs/cider/issues/3094
;;   (cons (concat "(let [__value "
;;                 (caar r)
;;                 "] (tap> {:clay-tap? true :form (quote " (caar r) ") :value __value}) __value)")
;;         (cdar r)))
;;
;; (advice-add 'cider-nrepl-request:eval
;; :filter-args #'cider-tap)
;; ```

;; ## Project Template
;; (coming soon)

;; ## Starting a Clay namespace

;; Now, we can write a namespace and play with Clay.

(ns intro
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kind :as kind]
            [scicloj.kindly.v3.kindness :as kindness]))

;; Let us start Clay.

(clay/start!)

;; The browser view should open automatically.

;; ## A few useful actions

;; Showing the whole namespace:
(comment
  (clay/show-doc! "notebooks/intro.clj"))

;; Writing the document:

(comment
  (do (clay/show-doc! "notebooks/intro.clj")
      (clay/write-html! "docs/index.html")))

;; ## Interaction

;; Clay listens to user evaluations and reflects them visually.

(+ 1 2)

;; In Emacs CIDER, after evaluation of a form (or a region),
;; the browser view should show the evaluation result.

;; In VSCode Calva, a similar effect can be achieved
;; using the dedicated command and keybinding defined above.

;; ## Kinds

;; The way things should be visualized is determined by the
;; [Kindly](https://github.com/scicloj/kindly) library.

;; Clay adopts Kindly's default advice, that will be demonstrated below.
;; However, user-defined Kindly advices should work as well.

;; Kindly advises tools (like Clay) about the kind of way a given context
;; things should be displayed, by assigning to it a so-called kind.

;; Please refer to the Kindly documentation for details about specifying
;; and using kinds.

;; ## Examples

;; ### Plain values

;; By default, when there is no kind information provided by Kindly,
;; values are pretty-printed.

{:a {:b (range 9)}}

;; ### Hiccup

[:p {:style ; https://www.htmlcsscolor.com/hex/7F5F3F
     {:color "#7F5F3F"}}
 "hello"]

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

(a-piece-of-random-art (+ 40 (rand-int 90)))

;; ### Tables

;; The `:kind/table` kind can be handy for an interactive table view.

(kind/table
 {:column-names [:preferred-language :age]
  :row-vectors (for [i (range 99)]
                 [(["clojure" "clojurescript" "babashka"]
                   (rand-int 3))
                  (rand-int 100)])})

(kind/table
 {:column-names [:preferred-language :age]
  :row-maps (for [i (range 99)]
              {:preferred-language (["clojure" "clojurescript" "babashka"]
                                    (rand-int 3))
               :age (rand-int 100)})})

;; ### Datasets

;; [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset) datasets use the default
;; printing of the library, which is then rendered as Markdown
;; (with some CSS styling).

;; Let us create such a dataset using [Tablecloth](https://github.com/scicloj/tablecloth).

(require '[tablecloth.api :as tc])

(-> {:x (range 6)
     :y [:A :B :C :A :B :C]}
    tc/dataset)

;; #### Known issues

;; With the current Markdown implementation, used by Clay (based on [Cybermonday](https://github.com/kiranshila/cybermonday)), brackets inside datasets cells are not visible.

(-> {:x [1 [2 3] 4]
     :y [:A :B :C]}
    tc/dataset)

;; For now, cases of this kind can be handled by the user by switching to the `:kind/pprint` kind.

(-> {:x [1 [2 3] 4]
     :y [:A :B :C]}
    tc/dataset
    kind/pprint)

;; ### [Vega](https://vega.github.io/vega/) and [Vega-Lite](https://vega.github.io/vega-lite/)

(defn vega-point-plot [data]
  (-> {:data {:values data},
       :mark "point"
       :encoding
       {:size {:field "w" :type "quantitative"}
        :x {:field "x", :type "quantitative"},
        :y {:field "y", :type "quantitative"},
        :fill {:field "z", :type "nominal"}}}
      kind/vega))

(defn random-data [n]
  (->> (repeatedly n #(- (rand) 0.5))
       (reductions +)
       (map-indexed (fn [x y]
                      {:w (rand-int 9)
                       :z (rand-int 9)
                       :x x
                       :y y}))))

(defn random-vega-plot [n]
  (-> n
      random-data
      vega-point-plot))

(random-vega-plot 9)

;; ### [Cytoscape.js](https://js.cytoscape.org/)

(def cytoscape-example
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

(kind/cytoscape cytoscape-example)

(kind/cytoscape [cytoscape-example
                 {:style {:height 100
                          :width 100}}])

;; ### [Apache Echarts](https://echarts.apache.org/)

(kind/echarts
 {:xAxis {:data ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]}
  :yAxis {}
  :series [{:type "bar"
            :color ["#7F5F3F"]
            :data [23 24 18 25 27 28 25]}]})

;; ## Extending Clay with new kinds and behaviours

;; (to be documented soon)

;; ## Delays

;; Clojure Delays are a common way to define computations that do not take place immediately. The computation takes place when dereferencing the value for the first time.

;; Clay makes sure to dererence Delays when passing values for visualization.

;; This is handy for slow example snippets and explorations, that one would typically not like to slow down the evaluation of the whole namespace, but would like to visualize them on demand and also include in them in the final document.

(delay
  (Thread/sleep 500)
  (+ 1 2))

(delay
  [:div [:big "hi......."]])

;; ## Tests

;; The `clay/check` function allows to define tests that render accordingly, marking their failure or success.

(-> 2
    (+ 3)
    (clay/check = 4))

(-> 2
    (+ 3)
    (clay/check = 5))

;; We are considering a so-called "doctest" setup involving such checks, so that actual Clojure tests would be derived automatically from them.

;; This would open the way for literate testing / testable documentation solutions, such as those we have been using in the past (e.g., in [tutorials](https://scicloj.github.io/clojisr/doc/clojisr/v1/tutorial-test/) of ClojisR using Notespace v2).

;; ## Nesting

;; Different kinds of views should (eventually) nest correctly, thanks to the nesting support of Portal, Clerk, etc.

;; Here are a few Vega-Lite specs (using the function defined above) inside a Hiccup block:

(->> [10 100 1000]
     (map (fn [n]
            [:div {:style {:width "400px"}}
             [:big (str "n=" n)]
             (random-vega-plot n)]))
     (into [:div])
     kind/hiccup)


;; ## Development

;; The Clay project was created using [build-clj](https://github.com/seancorfield/build-clj).

;; Run the project's tests.
;; ```
;; $ clojure -T:build test
;; ```
;; Run the project's CI pipeline and build a JAR.
;; ```
;; $ clojure -T:build ci
;; ```
;; Install it locally (requires the `ci` task be run first):
;; ```
;; $ clojure -T:build install
;; ```
;; Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment
;; variables (requires the `ci` task be run first):
;; ```
;; $ clojure -T:build deploy
;; ```

;; Your library will be deployed to org.scicloj/clay on clojars.org by default.

;; ## Planned features

;; - a quick-start project template

;; - client-server interactive widgets (for a proof-of-concept, see [the 2022-04-16 example project](https://github.com/scicloj/clay/tree/main/examples/example-project-20220416) and the [Clojure-Asia talk](https://www.youtube.com/watch?v=gFNPtgAw36k).)

;; - presentation slides

;; - more kinds of visualizations

:bye
