;; # Clay

^{:kindly/hide-code? true
  :kindly/kind :kind/hiccup}
[:img
 {:style {:width "100px"}
  :src "https://raw.githubusercontent.com/scicloj/clay/main/resources/Clay.svg.png"
  :alt "Clay logo"}]

;; [Clay](https://github.com/scicloj/clay) is a minimalistic Clojure tool for data visualization and literate programming, compatible with the [Kindly](https://scicloj.github.io/kindly/) convention.
;;

;; ## Status
;; This project will soon exit alpha-stage and have a stable release.
;;
;; Clay is developed by [Timothy Pratley](https://github.com/timothypratley/) & [Daniel Slutsky](https://github.com/daslu) in parallel and in coordination with [Claykind](https://github.com/timothypratley/claykind), a tool with similar goals which is build in a more thoughtful process, aiming at a more modular structure.

;; ## Goals

;; - Easily explore & share data visualizations and notebooks for others to easily pick & use.
;; - Encourage writing Kindly-compatible notes for future compatiblity with other tools.
;; - Flow with the REPL: encourage user interactions that flow naturally with the typical use of Clojure in editors and REPLs.

;; ## Video

^{:kindly/hide-code? true
  :kindly/kind :kind/hiccup}
[:iframe
 {:src "https://www.youtube.com/embed/HvhMsv3iVGM"
  :allowfullscreen "allowfullscreen"}]

^{:kindly/hide-code? true
  :kindly/kind :kind/hiccup}
[:iframe
 {:src "https://www.youtube.com/embed/X_SsjhmG5Ok"
  :allowfullscreen "allowfullscreen"}]



;; ## Setup

;; See [the example project](https://github.com/scicloj/clay/tree/main/examples/example-project) for a concrete example.

;; To enjoy Clay's dynamic interaction, you also need to inform it about code evaluations. This requires some setup at the your editor.
;;
;; To use [Quarto](https://quarto.org/)-related actions, it is necessary to have the Quarto CLI [installed](https://quarto.org/docs/get-started/) in your system.
;;
;; See the suggested setup for popular editors below. If your favourite editor is not supported yet, let us talk and make it work.

;; ### VSCode Calva

;; If you now run a REPL with Clay version in  your classpath, then Calva will have the relevant [custom REPL commands](https://calva.io/custom-commands/), as defined [here](https://github.com/scicloj/clay/blob/main/resources/calva.exports/config.edn).
;;
;; |name|function|
;; |--|--|
;; |`Clay make Namespace as HTML`|will genenrate an HTML rendering of the current namespace.
;; |`Clay make Namespace as Quarto, then HTML`|will generate a Quarto `.qmd` rendering of the current namespace, then render it as HTML through Quarto.|
;; |`Clay make Namespace as Quarto, then reveal.js`|will generate a Quarto `.qmd` rendering of the current namespace, then render it as a reveal.js slideshow through Quarto.|
;; |`Clay make current form as HTML`|will generate an HTML rendering of the current form, in the context of the current namespace.|

;; ### Emacs CIDER
;;
;; Please load [clay.el](https://github.com/scicloj/clay/blob/main/clay.el) at your Emacs config.
;;
;; It offers the following functions, that you may wish to create keybindings for:
;;
;; |name|function|
;; |--|--|
;; |`clay/start`|start clay if not started yet|
;; |`clay/make-ns-hmtl`|save clj buffer, render it as html, and show that in the browser view|
;; |`clay/make-ns-quarto-html`|save clj buffer, render it as quarto, render that as html, and show that in the browser view|
;; |`clay/make-ns-quarto-revealjs`|save clj buffer, render it as quarto, render that as a revealjs slideshow, and show that in the browser view|
;; |`clay/make-last-sexp`|render the last s-expression|
;; |`clay/make-defun-at-point`|render the [defun-at-point](https://www.emacswiki.org/emacs/ThingAtPoint)|

;; ### IntelliJ Cursive
;; **(coming soon)**

;; ## API

;; The entry point of the Clay API  is the `scicloj.clay.v2.api/make!` function.
;; Here are some usage examples.

(comment
  ;; Evaluate and render
  ;; the namespace in `"notebooks/index.clj"`
  ;; as HTML
  ;; and show it at the browser.
  (clay/make! {:format [:html]
               :source-path "notebooks/index.clj"})

  ;; Do the same as above
  ;; (since `:format [:html]` is the default).
  (clay/make! {:source-path "notebooks/index.clj"})

  ;; Evaluate and render
  ;; the namespace in `"notebooks/index.clj"`
  ;; as HTML
  ;; and do not show it at the browser.
  (clay/make! {:source-path "notebooks/index.clj"
               :show false})

  ;; Evaluate and render
  ;; the namespaces in `"notebooks/slides.clj"` `"notebooks/index.clj"`
  ;; as HTML
  ;; and do not show it at the browser.
  (clay/make! {:source-path ["notebooks/slides.clj"
                             "notebooks/index.clj"]
               :show false})

  ;; Evaluate and render a single form
  ;; in the context of the namespace in `"notebooks/index.clj"`
  ;; as HTML
  ;; and show it at the browser.
  (clay/make! {:source-path "notebooks/index.clj"
               :single-form '(kind/cytoscape
                              [{:style {:width "300px"
                                        :height "300px"}}
                               cytoscape-example])})

  ;; Evaluate and render a single form
  ;; in the context of the current namespace (`*ns*`)
  ;; as HTML
  ;; and show it at the browser.
  (clay/make! {:single-form '(kind/cytoscape
                              [{:style {:width "300px"
                                        :height "300px"}}
                               cytoscape-example])})

  ;; Render a single value
  ;; as HTML
  ;; and show it at the browser.
  (clay/make! {:single-value (kind/cytoscape
                              [{:style {:width "300px"
                                        :height "300px"}}
                               cytoscape-example])})

  ;; Evaluate and render
  ;; the namespace in `"notebooks/index.clj"`
  ;; as a Quarto qmd file
  ;; then, using Quarto, render that file as HTML
  ;; and show it at the browser.
  (clay/make! {:format [:quarto :html]
               :source-path "notebooks/index.clj"})

  ;; Evaluate and render
  ;; the namespace in `"notebooks/index.clj"`
  ;; as a Quarto qmd file
  ;; and show it at the browser.
  (clay/make! {:format [:quarto :html]
               :source-path "notebooks/index.clj"
               :run-quarto false})

  ;; Evaluate and render
  ;; the namespace in `"notebooks/slides.clj"`
  ;; as a Quarto qmd file
  ;; (using its namespace-specific config from the ns metadata)
  ;; then, using Quarto, render that file as HTML
  ;; and show it at the browser.
  (clay/make! {:format [:quarto :html]
               :source-path "notebooks/slides.clj"})

  ;; Evaluate and render
  ;; the namespace in `"notebooks/slides.clj"`
  ;; as a Quarto qmd file
  ;; (using its namespace-specific config from the ns metadata)
  ;; then, using Quarto, render that file as a reveal.js slideshow
  ;; and show it at the browser.
  (clay/make! {:format [:quarto :revealjs]
               :source-path "notebooks/slides.clj"})

  ;; Evaluate and render
  ;; the namespace in `"notebooks/index.clj"`
  ;; as a Quarto qmd file
  ;; with a custom Quarto config
  ;; then, using Quarto, render that file as HTML
  ;; and show it at the browser.
  (clay/make! {:format [:quarto :html]
               :source-path "notebooks/index.clj"
               :quarto {:highlight-style :nord}})

  ;; Evaluate and render
  ;; the namespace in `"index.clj"`
  ;; under the `"notebooks"` directory
  ;; as HTML
  ;; and show it at the browser.
  (clay/make! {:base-source-path "notebooks/"
               :source-path "index.clj"})

  ;; Create a Quarto book
  ;; (to be documented better soon).
  (clay/make! {:format [:quarto :html]
               :base-source-path "notebooks"
               :source-path ["index.clj"
                             "chapter.clj"]
               :base-target-path "book"
               :show false
               :run-quarto false
               :book {:title "Book Example"}})

  ,)

;; ## Configutation

;; Calls to the `make!` function are affected by various parameters
;; which collected as one nested map.
;; This map is the result of merging four sources:
;;
;; - the default configuration: [clay-default.edn](https://github.com/scicloj/clay/blob/main/resources/clay-default.edn) under Clay's resources
;;
;; - the user configuration: `clay.edn` at the top
;;
;; - the namespace configuration: the `:clay` member of the namespace metadata
;;
;; - the call configuration: the argument to `make!`
;;
;; Here are some of the parameters worth knowing about:
;;
;; **(to be documented soon)**

;; ## Starting a Clay namespace

;; Now, we can write a namespace and play with Clay.

(ns index
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.kindly.v4.kind :as kind]))

(defonce memoized-slurp
  (memoize slurp))

;; ## A few useful actions

;; Showing the whole namespace:
(comment
  (clay/show-doc! "notebooks/index.clj"))

;; Writing the document:
(comment
  (clay/show-doc-and-write-html!
   "notebooks/index.clj"
   {:toc? true}))

;; Reopening the Clay view in the browser (in case you closed the browser tab previously opened by `clay/start!`)
(comment
  (clay/browse!))

;; These can be conveniently bound to functions and keys at your editor (to b documented soon).

;; ## Interaction

;; Clay responds to user evaluations by displaying the result visually.

(+ 1111 2222)

;; In Emacs CIDER, after evaluation of a form (or a region),
;; the browser view should show the evaluation result.

;; In VSCode Calva, a similar effect can be achieved
;; using the dedicated command and keybinding defined above.

;; ## Kinds

;; The way things should be visualized is determined by the advice of
;; [Kindly](https://github.com/scicloj/kindly).

;; In this namespace we demonstrate Kindly's default advice.
;; User-defined Kindly advices should work as well.

;; Kindly advises tools (like Clay) about the kind of way a given context
;; should be displayed, by assigning to it a so-called kind.

;; Please refer to the Kindly documentation for details about specifying
;; and using kinds.

;; ## Examples

;; ### Plain values

;; By default, when there is no kind information provided by Kindly,
;; values are simply pretty-printed.

(+ 4 5)

(str "abcd" "efgh")

;; ### Hidden

;; Values of :kind/hidden are not shown.

(kind/hidden 9)

;; ### Hiccup

;; [Hiccup](https://github.com/weavejester/hiccup), a popular Clojure way to represent HTML, can be specified by kind:

(kind/hiccup
 [:ul
  [:li [:p "hi"]]
  [:li [:big [:big [:p {:style ; https://www.htmlcsscolor.com/hex/7F5F3F
                        {:color "#7F5F3F"}}
                    "hello"]]]]])

;; As we can see, this kind is displayed by converting Hiccup to HTML.

;; ### Reagent

(kind/reagent
 ['(fn [numbers]
     [:p {:style {:background "#d4ebe9"}}
      (pr-str (map inc numbers))])
  (vec (range 40))])

;; From the [reagent tutorial](https://reagent-project.github.io/):
(kind/reagent
 ['(fn []
     (let [*click-count (reagent.core/atom 0)]
       (fn []
         [:div
          "The atom " [:code "*click-count"] " has value: "
          @*click-count ". "
          [:input {:type "button" :value "Click me!"
                   :on-click #(swap! *click-count inc)}]])))])

;; [d3-require](https://github.com/d3/d3-require) can be used to provide js dependencies:

(kind/reagent
 ['(fn []
     (reagent.core/with-let
       [*result (reagent.core/atom nil)]
       (-> js/d3
           (.require "d3-array")
           (.then (fn [d3-array]
                    (reset! *result
                            (-> d3-array
                                (.range 9)
                                pr-str)))))
       [:pre @*result]))])

;; ### HTML

;; Raw html can be represented as a kind too:

(kind/html "<div style='height:40px; width:40px; background:purple'></div> ")

(kind/html
 "
<svg height=100 width=100>
<circle cx=50 cy=50 r=40 stroke='purple' stroke-width=3 fill='floralwhite' />
</svg> ")


;; ### Markdown

;; Markdown text (a vector of strings) can be handled using a kind too.

(kind/md
 "This is [markdown](https://www.markdownguide.org/).")

(kind/md
 ["
* This is [markdown](https://www.markdownguide.org/).
  * *Isn't it??*"
  "
* Here is **some more** markdown."])

;; When rendering through Quarto, LaTeX formulae are supported as well.

(kind/md
 "Let $x=9$. Then $$x+11=20$$")

;; ### Images

;; Java BufferedImage objects are displayed as images.

(import javax.imageio.ImageIO
        java.net.URL)

(defonce clay-image
  (->  "https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg"
       (URL.)
       (ImageIO/read)))

clay-image

;; ### Plain data structures

;; Plain data structures (lists and sequnces, vectors, sets, maps)
;; are pretty printed if there isn't any value inside
;; which needs to be displayed in special kind of way.

(def people-as-maps
  (->> (range 29)
       (mapv (fn [i]
               {:preferred-language (["clojure" "clojurescript" "babashka"]
                                     (rand-int 3))
                :age (rand-int 100)}))))

(def people-as-vectors
  (->> people-as-maps
       (mapv (juxt :preferred-language :age))))

(take 5 people-as-maps)

(take 5 people-as-vectors)

(->> people-as-vectors
     (take 5)
     set)

;; When something inside needs to be displayed in a special kind of way,
;; the data structures are printed in a way that makes that clear.

(def nested-structure-1
  {:vector-of-numbers [2 9 -1]
   :vector-of-different-things ["hi"
                                (kind/hiccup
                                 [:big [:big "hello"]])]
   :map-of-different-things {:markdown (kind/md ["*hi*, **hi**"])
                             :number 9999}
   :hiccup (kind/hiccup
            [:big [:big "bye"]])})

nested-structure-1


;; ### Pretty printing

;; The `:kind/pprint` kind  makes sure to simply pretty-print values:
(kind/pprint nested-structure-1)

;; ### Datasets

;; [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset) datasets currently use the default printing of the library,

;; Let us create such a dataset using [Tablecloth](https://github.com/scicloj/tablecloth).

(require '[tablecloth.api :as tc])

(-> {:x (range 6)
     :y [:A :B :C :A :B :C]}
    tc/dataset)

(-> {:x [1 [2 3] 4]
     :y [:A :B :C]}
    tc/dataset)

(-> [{:x 1 :y 2 :z 3}
     {:y 4 :z 5}]
    tc/dataset)

(-> people-as-maps
    tc/dataset)

;; ### Tables

;; The `:kind/table` kind can be handy for an interactive table view.

(kind/table
 {:column-names [:preferred-language :age]
  :row-vectors people-as-vectors})

(kind/table
 {:column-names [:preferred-language :age]
  :row-maps people-as-maps})

(kind/table
 {:column-names [:preferred-language :age]
  :row-maps (take 5 people-as-maps)})

(-> people-as-maps
    tc/dataset
    kind/table)

;; ### [Vega](https://vega.github.io/vega/) and [Vega-Lite](https://vega.github.io/vega-lite/)

(defn vega-lite-point-plot [data]
  (-> {:data {:values data},
       :mark "point"
       :encoding
       {:size {:field "w" :type "quantitative"}
        :x {:field "x", :type "quantitative"},
        :y {:field "y", :type "quantitative"},
        :fill {:field "z", :type "nominal"}}}
      kind/vega-lite))

(defn random-data [n]
  (->> (repeatedly n #(- (rand) 0.5))
       (reductions +)
       (map-indexed (fn [x y]
                      {:w (rand-int 9)
                       :z (rand-int 9)
                       :x x
                       :y y}))))

(defn random-vega-lite-plot [n]
  (-> n
      random-data
      vega-lite-point-plot))

(random-vega-lite-plot 9)

;; When the vega/vega-lite data is given in CSV format,
;; Clay will serve it in a separate CSV file alongside the generated HTML.

(-> {:data {:values "x,y
1,1
2,4
3,9
-1,1
-2,4
-3,9"
            :format {:type :csv}},
     :mark "point"
     :encoding
     {:x {:field "x", :type "quantitative"}
      :y {:field "y", :type "quantitative"}}}
    kind/vega-lite)


;; ### Cytoscape

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

(kind/cytoscape
 cytoscape-example)

(kind/cytoscape
 [{:style {:width "100px"
           :height "100px"}}
  cytoscape-example])

;; ### ECharts

;; This example is taken from Apache ECharts' [Getting Started](https://echarts.apache.org/handbook/en/get-started/).

(def echarts-example
  {:title {:text "Echarts Example"}
   :tooltip {}
   :legend {:data ["sales"]}
   :xAxis {:data ["Shirts", "Cardigans", "Chiffons",
                  "Pants", "Heels", "Socks"]}
   :yAxis {}
   :series [{:name "sales"
             :type "bar"
             :data [5 20 36
                    10 10 20]}]})

(kind/echarts
 echarts-example)

(kind/echarts
 [{:style {:width "500px"
           :height "200px"}}
  echarts-example])


;; ### Plotly
(kind/plotly
 {:data [{:x [0 1 3 2]
          :y [0 6 4 5]
          :z [0 8 9 7]
          :type :scatter3d
          :mode :lines+markers
          :opacity 0.5
          :line {:width 5}
          :marker {:size 4
                   :colorscale :Viridis}}]})

;; ### Leaflet

;; (experimental)

;; This example was adapted from [the Leaflet website](https://leafletjs.com/).

(kind/reagent
 ^{:deps [:leaflet]}
 ['(fn []
     [:div
      [:div {:style {:height "200px"}
             :ref (fn [el]
                    (let [m (-> js/L
                                (.map el)
                                (.setView (clj->js [51.505 -0.09])
                                          13))]
                      (-> js/L
                          (.tileLayer "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
                                      (clj->js
                                       {:maxZoom 19
                                        :attribution "&copy; <a href=\"http://www.openstreetmap.org/copyright\">OpenStreetMap</a>"}))
                          (.addTo m))
                      (-> js/L
                          (.marker (clj->js [51.5 -0.09]))
                          (.addTo m)
                          (.bindPopup "A pretty CSS popup.<br> Easily customizable.")
                          (.openPopup))))}]])])

;; ### 3DMol.js

;; Embedding a 3Dmol Viewer ([original example](https://3dmol.csb.pitt.edu/doc/tutorial-embeddable.html)):

(kind/reagent
 ^{:deps [:three-d-mol]}
 ['(fn [{:keys [data-pdb]}]
     [:div {:style {:height "400px"
                    :width "400px"
                    :position :relative}
            :class "viewer_3Dmoljs"
            :data-pdb data-pdb
            :data-backgroundcolor "0xffffff"
            :data-style "stick"
            :data-ui true}])
  {:data-pdb "2POR"}])

;; Using 3Dmol within your code (inspired by [these examples](https://3dmol.csb.pitt.edu/doc/tutorial-code.html)):

(kind/reagent
 ^{:deps [:three-d-mol]}
 ['(fn [{:keys [pdb-data]}]
     [:div
      {:style {:width "100%"
               :height "500px"
               :position "relative"}
       :ref (fn [el]
              (let [config (clj->js
                            {:backgroundColor "0xffffff"})
                    viewer (.createViewer js/$3Dmol el)]
                (.setViewStyle viewer (clj->js
                                       {:style "outline"}))
                (.addModelsAsFrames viewer pdb-data "pdb")
                (.addSphere viewer (clj->js
                                    {:center {:x 0
                                              :y 0
                                              :z 0}
                                     :radius 5
                                     :color "green"
                                     :alpha 0.2}))
                (.zoomTo viewer)
                (.render viewer)
                (.zoom viewer 0.8 2000)))}])
  {:pdb-data (memoized-slurp "https://files.rcsb.org/download/2POR.pdb")}])

;; ## Delays

;; Clojure Delays are a common way to define computations that do not take place immediately. The computation takes place when dereferencing the value for the first time.

;; Clay makes sure to dererence Delays when passing values for visualization.

;; This is handy for slow example snippets and explorations, that one would typically not like to slow down the evaluation of the whole namespace, but would like to visualize them on demand and also include in them in the final document.

(delay
  (Thread/sleep 500)
  (+ 1 2))

;; ## Embedded Portal

(kind/portal {:x (range 3)})

(kind/portal
 [(kind/hiccup [:img {:height 50 :width 50
                      :src "https://clojure.org/images/clojure-logo-120b.png"}])
  (kind/hiccup [:img {:height 50 :width 50
                      :src "https://raw.githubusercontent.com/djblue/portal/fbc54632adc06c6e94a3d059c858419f0063d1cf/resources/splash.svg"}])])

(kind/portal
 [(kind/hiccup [:big [:big "a plot"]])
  (random-vega-lite-plot 9)])

;; ## Nesting kinds in Hiccup (WIP)

(kind/hiccup
 [:div {:style {:background "#f5f3ff"
                :border "solid"}}

  [:hr]
  [:h3 [:code ":kind/md"]]
  (kind/md "*some text* **some more text**")

  [:hr]
  [:h3 [:code ":kind/code"]]
  (kind/code "{:x (1 2 [3 4])}")

  [:hr]
  [:h3 [:code "kind/dataset"]]
  (tc/dataset {:x (range 33)
               :y (map inc (range 33))})

  [:hr]
  [:h3 [:code "kind/table"]]
  (kind/table
   (tc/dataset {:x (range 33)
                :y (map inc (range 33))}))

  [:hr]
  [:h3 [:code "kind/vega-lite"]]
  (random-vega-lite-plot 9)

  [:hr]
  [:h3 [:code "kind/vega-lite"]]
  (-> {:data {:values "x,y
1,1
2,4
3,9
-1,1
-2,4
-3,9"
              :format {:type :csv}},
       :mark "point"
       :encoding
       {:x {:field "x", :type "quantitative"}
        :y {:field "y", :type "quantitative"}}}
      kind/vega-lite)

  [:hr]
  [:h3 [:code "kind/reagent"]]
  (kind/reagent
   ['(fn [numbers]
       [:p {:style {:background "#d4ebe9"}}
        (pr-str (map inc numbers))])
    (vec (range 40))])])


;; ## Nesting kinds in Tables (WIP)

(kind/table
 {:column-names [:x :y]
  :row-vectors [[(kind/md "*some text* **some more text**")
                 (kind/code "{:x (1 2 [3 4])}")]
                [(tc/dataset {:x (range 3)
                              :y (map inc (range 3))})
                 (random-vega-lite-plot 9)]]})

;; ## More nesting examples

{:plot (random-vega-lite-plot 9)
 :dataset (tc/dataset {:x (range 3)
                       :y (repeatedly 3 rand)})}

[(random-vega-lite-plot 9)
 (tc/dataset {:x (range 3)
              :y (repeatedly 3 rand)})]

;; ## Referring to files

;; In data visualizations, one can directly refrer to files places under `"notebooks/"` or `"src/"`.

(kind/hiccup
 [:img {:src "notebooks/images/Clay.svg.png"}])

(kind/vega-lite
 {:data {:url "notebooks/datasets/iris.csv"},
  :mark "rule",
  :encoding {:opacity {:value 0.2}
             :size {:value 3}
             :x {:field "sepal_width", :type "quantitative"},
             :x2 {:field "sepal_length", :type "quantitative"},
             :y {:field "petal_width", :type "quantitative"},
             :y2 {:field "petal_length", :type "quantitative"},
             :color {:field "species", :type "nominal"}}
  :background "floralwhite"})

;; ## Coming soon

;; In the past, Clay used to support various data visualization libraries such as MathBox.
;;
;; These have been disabled in a recent refactoring (Oct. 2023) and will be brought back soon.
