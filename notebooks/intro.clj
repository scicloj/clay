;; # Clay

;; ![quaternary clay in Estonia](https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg)
;; (credit: [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:Clay-ss-2005.jpg))

;; ## What is it?

;; [Clay](https://github.com/scicloj/clay) is a tiny Clojure tool offering a dynamic workflow using some of the more serious visual tools such as [Portal](https://github.com/djblue/portal) and [Clerk](https://github.com/nextjournal/clerk). Here, by visual tools we mean tools for data visualization and literate programming.

;; It is one of the fruits of our explorations at the [visual-tools-group](https://scicloj.github.io/docs/community/groups/visual-tools/).

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
;; - [Viz.clj](https://github.com/scicloj/viz.clj) - a (work-in-progress) library for easy data visualizations, which is Kindly-aware, and thus fits nicely with Clay

;; ## Setup

;; For Clay to work, it is necessary to add its relevant nREPL middleware, typically through an alias of the `deps.edn` file. See [the example project](https://github.com/scicloj/clay/tree/main/examples/example-project).

;; ```clj
;; :aliases {:clay
;;           {:extra-deps {cider/cider-nrepl {:mvn/version "0.28.3"}}
;;            :main-opts
;;            ["-m" "nrepl.cmdline"
;;             "--middleware" "[scicloj.clay.v1.nrepl/middleware,cider.nrepl/cider-middleware]"
;;             "-i"]}}
;; ```

;; Then, one needs to run their REPL with the defined `clay` alias, using the `-M:clay` option. In Calva, this will be offered automatically on jack-in. In CIDER, this can be configured using [.dir-locals.el](https://www.gnu.org/software/emacs/manual/html_node/emacs/Directory-Variables.html) -- see the example project above.

;; Now, we can write a namespace and play with Clay.

(ns intro
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.clay.v1.tools :as tools]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind]
            [scicloj.kindly.v2.kindness :as kindness]
            [nextjournal.clerk :as clerk]))

;; Clay can be started with the choice of desired tools to use:

(clay/start! {:tools [tools/clerk
                      tools/portal
                      tools/scittle]})

;; The view of those tools should open automatically (e.g., a Portal window and a Clerk tab in the browser).

;; Clay can also be restarted with a new choice of tools:

(comment
  (clay/restart! {:tools [tools/clerk
                          tools/portal
                          tools/scittle]}))

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

;; ### Kinds through metadata

(def some-hiccup
  [:p {:style {:color "#c9a465"}}
   [:big "hello"]])

;; Let us specify the kind for this value. We'll do it in three ways: by calling a kind, by considering it (using `kindly/consider`), or by considering the corresponding keyword.

[(-> some-hiccup kind/hiccup)
 (-> some-hiccup (kindly/consider kind/hiccup))
 (-> some-hiccup (kindly/consider :kind/hiccup))]

;; Notice how in all cases, the view is affected accordingly, as we just told Clay to treat the value as [Hiccup](https://github.com/weavejester/hiccup) notation for rendering HTML.

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


;; ### Kinds through protocols

;; Another way of specifying kind is implementing the Kindness protocol. For example, the Java `BufferedImage` class implements it in order to support viewing images.

(import java.awt.image.BufferedImage)

(->> (BufferedImage. 16 16 BufferedImage/TYPE_INT_RGB)
     (satisfies? kindness/Kindness))

(-> (BufferedImage. 16 16 BufferedImage/TYPE_INT_RGB)
    kindly/kind)

;; ## Useful kinds defined in Clay

;; ### Naive

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

(repeatedly 4 #(a-piece-of-random-art (+ 20 (rand-int 90))))

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

;; $ clojure -T:build test

;; Run the project's CI pipeline and build a JAR.

;; $ clojure -T:build ci

;; Install it locally (requires the `ci` task be run first):

;; $ clojure -T:build install

;; Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment
;; variables (requires the `ci` task be run first):

;; $ clojure -T:build deploy

;; Your library will be deployed to org.scicloj/clay on clojars.org by default.

;; ## Planned features

;; One of the things we are experimenting with is an additional Tool for plain html rendering, that would allow rendering static web pages, presentation slides, etc., and also conveniently embedding views of many Javascript visualization libraries with no trouble.

;; Such functionality is supported by the upcoming version 2 of [Oz](https://github.com/metasoarous/oz), which could be considered as a Clay Tool as well.

;; We are also looking into creating a template that would allow users to easily create projects with Clay.

:bye

(comment
  (clerk/build-static-app!
   {:paths ["notebooks/intro.clj"]}))
