;; # Clay

;; ![quaternary clay in Estonia](https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg)
;;
;; (credit: [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:Clay-ss-2005.jpg))

;; ## What is it?

;; [Clay](https://github.com/scicloj/clay) is a Clojure tool for data visualization and literate programming, offering a dynamic workflow compabible with popular visual tools & libraries such as [Portal](https://github.com/djblue/portal), [Clerk](https://github.com/nextjournal/clerk), and [Scittle](https://github.com/babashka/scittle). Here, by visual tools we mean tools for data visualization and literate programming.

;; It is one of the fruits of our explorations at the [visual-tools-group](https://scicloj.github.io/docs/community/groups/visual-tools/).

;; This document has been created using Clay.

;; ### Goals

;; * *Easily* **explore & share** things for others to *easily* **pick & use**.

;; Small examples, library tutorials, research reports, etc., should be easily shareable as code that renders to visual documents. Not only sharing things should be obvious, but also using the shared things.

;; * In **common** use cases, have **compatible** experiences at the relevant tools.

;; Common visualizations such as images, tables, Vega/Vega-Lite plots, plain Clojure data structures, etc., should be describeable in a way that can just work in all relevant tools (e.g., Portal, Clerk) without any code change between tools.

;; * In **all** use cases, be able to use **the best** tools.

;; Achieving such compatibility across tools in common cases should not limit the use of the tools in any case. In all cases, it should be possible to use the best of the amazing features of tools such as Portal & Clerk.

;; * **Flow** with the REPL.

;; We wish to encourage user interactions that flow naturally with the typical use of Clojure in editors and REPLs, editing and evaluating Clojure forms.

;; ### The stack

;; Clay is part of a stack of libraries seeking easy experience with common data-centric tasks.
;; - [Kindly](https://github.com/scicloj/kindly) - a tiny library  for specifying the kind of way different things should be viewed
;; - [Clay](https://github.com/scicloj/clay) - a dynamic workflow for visual exploration & documentation, combining different tools using Kindly
;; - [Viz.clj](https://github.com/scicloj/viz.clj) - a (work-in-progress) library for easy data visualizations on top of [Hanami](https://github.com/jsa-aerial/hanami), which is Kindly-aware, and thus fits nicely with Clay

;; ## Setup

;; For Clay to work, we need to be able to inform it about code evaluations. This requires some setup specific to your editor.
;; (This is needed only for enjoying Clay's dynamic interaction. For rendering of a whole document, it is not needed.)

;; If your Clojure environment is not supported yet, let us talk and make it work.

;; ### Calva

;; Please add the following command to your keybindings (you may pick another key, of course). This command would evaluate a piece of code and send the result to be visualized in Clay.

;; ```json
;; {
;;  "key": "ctrl+shift+enter",
;;  "command": "calva.runCustomREPLCommand",
;;  "args": "(tap> {:clay-tap? true :code-meta (meta (quote $current-form)) :code (str (quote $current-form)) :value $current-form})"
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
;;                 "] (tap> {:clay-tap? true :code-meta (meta (quote " (caar r) ")) :value __value}) __value)")
;;         (cdar r))))
;;
;; (advice-add 'cider-nrepl-request:eval
;; :filter-args #'cider-tap)
;; ```

;; ## Project Template
;; (coming soon)

;; ## Starting a Clay namespace

;; Now, we can write a namespace and play with Clay.

(ns intro
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.clay.v1.tools :as tools]
            [scicloj.clay.v1.extensions :as extensions]
            [scicloj.clay.v1.tool.scittle :as scittle]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind]
            [scicloj.kindly.v2.kindness :as kindness]
            [nextjournal.clerk :as clerk]))

;; Clay can be started with the choice of desired tools to use, as well as extensions that take care of viewing certain datatypes appropriately.

(clay/start! {:tools [tools/clerk
                      tools/portal
                      tools/scittle]
              :extensions [extensions/dataset
                           extensions/clojisr]})

;; The view of those tools should open automatically (e.g., a Portal window, a Clerk tab in the browser, and a Clay tab with a Scittle-based HTML page).

;; ## A few useful actions

;; Restarting with a new choice of tools and extensions:

(comment
  (clay/restart! {:tools [tools/scittle]
                  :extensions [extensions/dataset
                               extensions/clojisr]}))

;; Showing the usual Clerk view of a whole namespace:

(comment
  (kind/hidden
   [(clerk/show! "notebooks/intro.clj")]))

;; Static rendering in Clerk:

(comment
  (clerk/build-static-app! {:paths ["notebooks/intro.clj"]}))

;; Showing the whole namespace using Scittle:
(comment
  (scittle/show-doc! "notebooks/intro.clj"))

;; Writing the Scittle-based document:

(comment
  (do (scittle/show-doc! "notebooks/intro.clj")
      (scittle/write-html! "docs/index.html")))

;; ## Tools

;; Tools (e.g., `tools/clerk` and `tools/portal` above) are reifications of the `Tool` protocol, that defines common behaviours of tools.

;; ## Interaction

;; Clay is not supposed to interfere with the usual way of using tools. One can keep submitting values to Portal, showing files in Clerk, evaluating forms in CIDER or Calva, etc.

;; But Clay adds something: it listens to user evaluations and reflects them visually in the chosen tools.

;; If one has started Clay with `tools/clerk` and `tools/portal`, then after evaluation, the corresponding views (Clerk browser tab, Portal window) should show the evaluation result.

(+ 1 2)

;; The way things should be visualized is determined by their so-called kinds.

;; ## Kinds

;; The Kindly library allows one to attach kinds to things. Those kinds can have certain behaviours defined for different tools. Thus, the specified kinds determine the way things are viewed.

;; One can check the kind of a value using the `kindly/kind` function.

;; There are a few ways to attach a kind to a value.

;; ### Value kind metadata

;; Let us look into this example in [Hiccup](https://github.com/weavejester/hiccup) notation.

(def some-hiccup
  [:p {:style {:color "#c9a465"}}
   [:big "hello"]])

;; We wish to tell Clay that it is of kind `hiccup`.

;; Let us specify the kind for this value. We'll do it in three ways: by calling a kind, by considering it (using `kindly/consider`), or by considering the corresponding keyword.

[(-> some-hiccup kind/hiccup)
 (-> some-hiccup (kindly/consider kind/hiccup))
 (-> some-hiccup (kindly/consider :kind/hiccup))]

;; Notice how in all cases, the view is affected accordingly, as we just told Clay to treat the value as Hiccup notation for rendering HTML.

;; Let us check the kind in all these cases:

(->> [(-> some-hiccup kind/hiccup)
      (-> some-hiccup (kindly/consider kind/hiccup))
      (-> some-hiccup (kindly/consider :kind/hiccup))]
     (map kindly/kind))

;; Actually, in all these cases, the kind is represeted using metadata attached to the value:

(->> [(-> some-hiccup kind/hiccup)
      (-> some-hiccup (kindly/consider kind/hiccup))
      (-> some-hiccup (kindly/consider :kind/hiccup))]
     (map (fn [v]
            (-> v
                meta
                :kindly/kind))))

;; ### Code kind metadata

;; Kind metadata can also attached to the code itself (rather than the resulting value).

^:kind/hiccup
some-hiccup

^{:kind/hiccup true}
some-hiccup

;; (This option currently does not work in the `clerk` tool.)

;; ### Kindness protocol

;; Another way of specifying kind is implementing the `Kindness` protocol. For example, the Java `BufferedImage` class implements it in order to support viewing images.

(import java.awt.image.BufferedImage)

(->> (BufferedImage. 16 16 BufferedImage/TYPE_INT_RGB)
     (satisfies? kindness/Kindness))

(-> (BufferedImage. 16 16 BufferedImage/TYPE_INT_RGB)
    kindly/kind)

;; ## Useful kinds defined in Clay

;; ### Naive

;; The naive kind just uses the usual printing of the value, in any tool.

(-> {:x 9}
    kind/naive)

;; ### Hiccup

(-> [:p {:style ; https://www.htmlcsscolor.com/hex/7F5F3F
         {:color "#7F5F3F"}}
     "hello"]
    kind/hiccup)

;; ### Images

;; Images are handled automatically (technically, this works since the `BufferedImage` class implements the `Kindness` protocol).

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

;; ### Datasets
;; For [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset) datasets to render coodectly, it is necessary to use the `dataset` extension when starting Clay (see above), and to have tech.ml.dataset as a dependency of the project.

;; In this example, let us create a dataset using [Tablecloth](https://github.com/scicloj/tablecloth).

(require '[tablecloth.api :as tc])

(-> {:x (range 6)
     :y [:A :B :C :A :B :C]}
    tc/dataset)

;; #### Known issues

;; With the current Markdown implementation used by `tool/scittle` (based on [Cybermonday](https://github.com/kiranshila/cybermonday)), brackets inside datasets cells are not visible.

(-> {:x [1 [2 3] 4]
     :y [:A :B :C]}
    tc/dataset)

;; For now, cases of this kind can be handled by the user by naive rendering of the printed output:

(-> {:x [1 [2 3] 4]
     :y [:A :B :C]}
    tc/dataset
    kind/naive)

;; ### RObjects (ClijisR)

;; For [ClojisR](https://github.com/scicloj/clojisr) `RObjects` (which are Clojure handles to objects of the R language) to render coodectly, it is necessary to use the `clojisr` extension when starting Clay (see above), and to have ClojisR as a dependency of the project.

(require '[clojisr.v1.r :as r])

(r/r '(rnorm 9))

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
  (-> [:div [:big "hi......."]]
      (kindly/consider kind/hiccup)))

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

(-> (->> [10 100 1000]
         (map (fn [n]
                [:div {:style {:width "400px"}}
                 [:big (str "n=" n)]
                 (vega-lite-spec n)]))
         (into [:div]))
    (kindly/consider kind/hiccup))

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
