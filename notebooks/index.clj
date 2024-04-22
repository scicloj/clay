^{:kindly/hide-code true
  :kindly/kind :kind/hiccup}
[:img
 {:style {:width "100px"}
  :src "https://raw.githubusercontent.com/scicloj/clay/main/resources/Clay.svg.png"
  :alt "Clay logo"}]

;; ## About

;; [Clay](https://github.com/scicloj/clay) is a minimalistic Clojure tool for data visualization and literate programming, compatible with the [Kindly](https://scicloj.github.io/kindly-noted/kindly) convention.
;; It allows to conduct visual data explorations and create documents (HTML pages like this one, books, blog posts, reports, slideshows) from source code and comments.
;;
;;
;; **Source:** [![(GitHub repo)](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/scicloj/clay)
;;
;; **Artifact:** [![(Clojars coordinates)](https://img.shields.io/clojars/v/org.scicloj/clay.svg)](https://clojars.org/org.scicloj/clay)
;;
;; **Status:** The project has moved into Beta stage (March 2024).
;;
;;
;; Clay is developed by [Timothy Pratley](https://github.com/timothypratley/) & [Daniel Slutsky](https://github.com/daslu) in parallel and in coordination with [Claykind](https://github.com/timothypratley/claykind), a tool with similar goals which is build in a more thoughtful process, aiming at a more modular structure.

;; ## Goals

;; - Easily explore & share data visualizations and notebooks for others to easily pick & use.
;; - Encourage writing Kindly-compatible notes for future compatiblity with other tools.
;; - Flow with the REPL: encourage user interactions that flow naturally with the typical use of Clojure in editors and REPLs.

;; ## Getting started

;; Add Clay to your project dependencies:
;; [![(Clojars coordinates)](https://img.shields.io/clojars/v/org.scicloj/clay.svg)](https://clojars.org/org.scicloj/clay)
;;
;; (If you like to use [aliases](https://practical.li/blog-staging/posts/clojure-cli-tools-understanding-aliases/), you may add under it to the extra dependencies under an alias. See, for example, the [deps.edn](https://github.com/scicloj/noj/blob/main/deps.edn) file of [Noj](https://github.com/scicloj/noj). If you do not know what this means, just add it under the main `:deps` section of your `deps.edn` file.)
;;
;; To render a given Clojure namespace, say `"notebooks/index.clj"`, you may run the following in the REPL:
^{:kindly/hide-code true
  :kindly/kind :kind/code}
["(require '[scicloj.clay.v2.api :as clay])
(clay/make! {:source-path \"notebooks/index.clj\"})"]
;; This will render an HTML page and serve it in Clay's browser view.
;; Note that Clay does not need to be mentioned in the namespace we wish to render.
;;
;; See the [API](index.html#api) and [Configuration](index.html#configuration) subsections for more options and variations.
;;
;; See the [Setup](index.html#setup) section and recent [Videos](index.html#videos) for details about integrating Clay with your editor so you do not need to call `make!` yourself.

;; ## Projects using Clay

;; - [Tablecloth documentation](https://scicloj.github.io/tablecloth/)
;; - [Clay documentation](https://scicloj.github.io/clay/)
;; - [Kindly-noted](https://scicloj.github.io/kindly-noted/) - documenting the ecosystem around Kindly - WIP
;; - [Noj documentation](https://scicloj.github.io/noj/) - WIP
;; - [Clojure Tidy Tuesdays](https://kiramclean.github.io/clojure-tidy-tuesdays/) data-science explorations
;; - [Clojure Data Scrapbook](https://scicloj.github.io/clojure-data-scrapbook/)
;; - [LLMs tutorial](https://kpassapk.github.io/llama.clj/llama.html) (in spanish) by Kyle Passarelli

;; ## Videos

^{:kindly/hide-code true
  :kindly/kind :kind/hiccup}
(->> [["June 10th 2023"
       "An early overview - babashka-conf"
       "HvhMsv3iVGM"]
      ["Dec. 1st 2023"
       "Kindly & Clay overview - visual-tools group - see Daniel's & Tim's parts"
       "DAQnvAgBma8"]
      ["Dec. 12th 2023"
       "Demo & Clay overview - London Clojurians - see Tim's part"
       "skMMvxWjmNM"]
      ["Dec. 16th 2023"
       "Calva integration - datavis demo"
       "X_SsjhmG5Ok"]
      ["Dec. 17th 2023"
       "CIDER integration - image processing demo"
       "fd4kjlws6Ts"]
      ["Dec. 17th 2023"
       "Cursive integration, API, configuration - blogging demo"
       "GsML75MtNXw"]]
     reverse
     (map (fn [[date title youtube-id]]
            [:tr
             [:td date]
             [:td title]
             [:td ^:kind/video {:youtube-id youtube-id}]]))
     (into [:table]))

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
;; See the [clay.el](https://github.com/scicloj/clay.el) package for the relevant interactive functions.

;; ### IntelliJ Cursive
;;
;; Under preferences, search for "REPL Commands"
;; (or use the menu IntelliJ -> Preferences -> Languages and Frameworks -> Clojure -> REPL Commands)
;;
;; Add a global command, and edit it with these settings:
;;
;; **Name:** Send form to Clay\
;; **Execution:** Command
;;
^{:kind/code true
  :kindly/hide-code true}
["(do (require '[scicloj.clay.v2.api :as clay])
    (clay/make! {:single-form '~form-before-caret
                 :source-path [\"~file-path\"]}))"]
;;
;; You might also like to create a command to compile the namespace:
;;
^{:kind/code true
  :kindly/hide-code true}
["(do (require '[scicloj.clay.v2.api :as clay])
    (clay/make! {:source-path [\"~file-path\"]}))"]
;;
;; Or a `top-level-form` (replace `form-before-caret` with `top-level-form`).
;;
;; You can then add keybindings under Preferences -> Keymap for the new commands.
;;
;; For more information about commands, see the Cursive documentation on [REPL commands and substitutions](https://cursive-ide.com/userguide/repl.html#repl-commands).

;; ## Example notebook namespace

;; This notebook is created by [a Clojure namespace](https://github.com/scicloj/clay/blob/main/notebooks/index.clj).
;; Here is the namespace definition and a few examples of what such a namespace may contain.

(ns index
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]
            [tablecloth.api :as tc]
            [scicloj.metamorph.ml :as ml]
            [scicloj.metamorph.ml.toydata :as toydata]
            [scicloj.noj.v1.stats :as noj.stats]
            [scicloj.noj.v1.vis.hanami :as hanami]
            [aerial.hanami.templates :as ht]
            [scicloj.clay.v2.quarto.themes :as quarto.themes]
            [scicloj.clay.v2.quarto.highlight-styles :as quarto.highlight-styles]
            [clojure.math :as math]))

;; A Hiccup spec:
(kind/hiccup
 [:div {:style {:background "#efe9e6"
                :border-style :solid}}
  [:ul
   [:li "one"]
   [:li "two"]
   [:li "three"]]])

;; A dataset using [Tablecloth](https://scicloj.github.io/tablecloth/):
(-> {:x (range 5)
     :y (repeatedly 5 rand)}
    tc/dataset
    (tc/set-dataset-name "my dataset"))

;; A plot using [Hanami](https://github.com/jsa-aerial/hanami) and [Noj](https://scicloj.github.io/noj/):

(-> (toydata/iris-ds)
    (hanami/plot ht/rule-chart
                 {:X "sepal_width"
                  :X2 "sepal_length"
                  :Y "petal_width"
                  :Y2 "petal_length"
                  :COLOR "species"
                  :SIZE 3
                  :OPACITY 0.2}))

;; ## API

(require '[scicloj.clay.v2.api :as clay])

;; The entry point of the Clay API  is the `scicloj.clay.v2.api/make!` function.
;; Here are some usage examples.

;; Evaluate and render
;; the namespace in `"notebooks/index.clj"`
;; as HTML
;; and show it at the browser:
(comment
  (clay/make! {:format [:html]
               :source-path "notebooks/index.clj"}))

;; Do the same as above
;; (since `:format [:html]` is the default):
(comment
  (clay/make! {:source-path "notebooks/index.clj"}))

;; Evaluate and render
;; the namespace in `"notebooks/index.clj"`
;; as HTML
;; and do not show it at the browser:
(comment
  (clay/make! {:source-path "notebooks/index.clj"
               :show false}))

;; Evaluate and render
;; the namespaces in `"notebooks/slides.clj"` `"notebooks/index.clj"`
;; as HTML
;; and do not show it at the browser:
(comment
  (clay/make! {:source-path ["notebooks/slides.clj"
                             "notebooks/index.clj"]
               :show false}))

;; Evaluate and render a single form
;; in the context of the namespace in `"notebooks/index.clj"`
;; as HTML
;; and show it at the browser:
(comment
  (clay/make! {:source-path "notebooks/index.clj"
               :single-form '(kind/cytoscape
                              cytoscape-example
                              {:element/style {:width "300px"
                                               :height "300px"}})}))

;; Evaluate and render a single form
;; in the context of the current namespace (`*ns*`)
;; as HTML
;; and show it at the browser:
(comment
  (clay/make! {:single-form '(kind/cytoscape
                              cytoscape-example
                              {:element/style {:width "300px"
                                               :height "300px"}})}))

;; Render a single value
;; as HTML
;; and show it at the browser:
(comment
  (clay/make! {:single-value (kind/cytoscape
                              cytoscape-example
                              {:element/style {:width "300px"
                                               :height "300px"}})}))

;; Evaluate and render
;; the namespace in `"notebooks/index.clj"`
;; as a Quarto qmd file
;; then, using Quarto, render that file as HTML
;; and show it at the browser:
(comment
  (clay/make! {:format [:quarto :html]
               :source-path "notebooks/index.clj"}))

;; Evaluate and render
;; the namespace in `"notebooks/index.clj"`
;; as a Quarto qmd file
;; and show it at the browser:
(comment
  (clay/make! {:format [:quarto :html]
               :source-path "notebooks/index.clj"
               :run-quarto false}))

;; Evaluate and render
;; the namespace in `"notebooks/slides.clj"`
;; as a Quarto qmd file
;; (using its namespace-specific config from the ns metadata)
;; then, using Quarto, render that file as HTML
;; and show it at the browser:
(comment
  (clay/make! {:format [:quarto :html]
               :source-path "notebooks/slides.clj"}))

;; Evaluate and render
;; the namespace in `"notebooks/slides.clj"`
;; as a Quarto qmd file
;; (using its namespace-specific config from the ns metadata)
;; then, using Quarto, render that file as a reveal.js slideshow
;; and show it at the browser:
(comment
  (clay/make! {:format [:quarto :revealjs]
               :source-path "notebooks/slides.clj"}))

;; Evaluate and render
;; the namespace in `"notebooks/index.clj"`
;; as a Quarto qmd file
;; with a custom Quarto config
;; then, using Quarto, render that file as HTML
;; and show it at the browser:
(comment
  (clay/make! {:format [:quarto :html]
               :source-path "notebooks/index.clj"
               :quarto {:highlight-style :nord
                        :format {:html {:theme :journal}}}}))

;; Evaluate and render
;; the namespace in `"notebooks/index.clj"`
;; as a Quarto qmd file
;; with a custom Quarto config
;; where the higlight style is fetched from
;; the `scicloj.clay.v2.quarto.highlight-styles` namespace,
;; and the theme is fetched from
;; the `scicloj.clay.v2.quarto.themes` namespace,
;; then, using Quarto, render that file as HTML
;; and show it at the browser:
(comment
  (require '[scicloj.clay.v2.quarto.highlight-styles :as quarto.highlight-styles]
           '[scicloj.clay.v2.quarto.themes :as quarto.themes])
  (clay/make! {:format [:quarto :html]
               :source-path "notebooks/index.clj"
               :quarto {:highlight-style quarto.highlight-styles/nord
                        :format {:html {:theme quarto.themes/journal}}}}))

;; Evaluate and render
;; the namespace in `"index.clj"`
;; under the `"notebooks"` directory
;; as HTML
;; and show it at the browser:
(comment
  (clay/make! {:base-source-path "notebooks/"
               :source-path "index.clj"}))

;; Create a Quarto book
;; with a default generated index page:
(comment
  (clay/make! {:format [:quarto :html]
               :base-source-path "notebooks"
               :source-path ["chapter.clj"
                             "another_chapter.md"
                             "a_chapter_with_R_code.Rmd"
                             "test.ipynb"]
               :base-target-path "book"
               :book {:title "Book Example"}
               ;; Empty the target directory first:
               :clean-up-target-dir true}))


;; Create a Quarto book
;; with a specified index page:
(comment
  (clay/make! {:format [:quarto :html]
               :base-source-path "notebooks"
               :source-path ["index.clj"
                             "chapter.clj"
                             "another_chapter.md"]
               :base-target-path "book"
               :book {:title "Book Example"}
               ;; Empty the target directory first:
               :clean-up-target-dir true}))

;; Reopen the Clay view in the browser
;; (in case you closed the browser tab previously opened):

(comment
  (clay/browse!))

;; ## Configuration

;; Calls to the `make!` function are affected by various parameters
;; which collected as one nested map.
;; This map is the result of deep-merging four sources:
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


;; ## Kinds

;; The way things should be visualized is determined by the advice of
;; [Kindly](https://scicloj.github.io/kindly-noted/kindly).

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

;; One current known issue is that LaTeX would not render correctly
;; when nesting `kind/md` inside other kinds.

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
            [:big [:big "bye"]])
   :dataset (tc/dataset {:x (range 3)
                         :y [:A :B :C]})})

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

;; The `:kind/table` kind can be handy for an interactive table view. `:kind/table` understands many structures which can be rendered as a table.

;; A map containing either `:row-vectors` (sequence of sequences) or `row-maps` (sequence of maps) keys with optional `:column-names`.

(kind/table
 {:column-names [:preferred-language :age]
  :row-vectors people-as-vectors})

;; Lack of column names produces table without a header.

(kind/table
 {:row-vectors (take 5 people-as-vectors)})

;; Column names are inferred from a sequence of maps

(kind/table
 {:row-maps (take 5 people-as-maps)})

;; We can limit displayed columns for sequence of maps case.

(kind/table
 {:column-names [:preferred-language]
  :row-maps (take 5 people-as-maps)})

;; Sequence of sequences and sequence of maps also work

(kind/table (take 5 people-as-vectors))

(kind/table (take 5 people-as-maps))

;; Additionally map of sequences is supported (unless it contains `:row-vectors` or `:row-maps` key, see such case above).

(kind/table {:x (range 6)
             :y [:A :B :C :A :B :C]})

;; A dataset can be also treated as a table input.

(def people-as-dataset
  (tc/dataset people-as-maps))

(-> people-as-dataset
    kind/table)

;; Additional options may hint at way the table should be rendered.
(-> people-as-dataset
    (kind/table {:element/max-height "300px"}))

;; It is possible to use [datatables](https://datatables.net/) to reneder `kind/table`,
;; and in this case the user may specify [datatables options](https://datatables.net/manual/options)
;; (see [the full list](https://datatables.net/reference/option/)).

(-> people-as-maps
    tc/dataset
    (kind/table {:use-datatables true}))

(-> people-as-dataset
    (kind/table {:use-datatables true
                 :datatables {:scrollY 300}}))

;; ### ML models

(-> (toydata/iris-ds)
    (noj.stats/linear-regression-model :sepal_length
                                       [:sepal_width
                                        :petal_width
                                        :petal_length])
    ml/thaw-model)

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

;; See the [Cytoscape docs](https://cytoscape.org/).

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

(-> cytoscape-example
    (kind/cytoscape {:element/style
                     {:width "100px"
                      :height "100px"}}))

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

(-> echarts-example
    (kind/echarts {:element/style
                   {:width "500px"
                    :height "200px"}}))

;; ### Plotly

;; See [the plotly.js docs](https://plotly.com/javascript/).

(def plotly-example
  {:data [{:x [0 1 3 2]
           :y [0 6 4 5]
           :z [0 8 9 7]
           :type :scatter3d
           :mode :lines+markers
           :opacity 0.5
           :line {:width 5}
           :marker {:size 4
                    :colorscale :Viridis}}]
   :layout {:title "Plotly example"}})

(kind/plotly
 plotly-example)

(-> plotly-example
    (kind/plotly {:element/style
                  {:width "300px"
                   :height "300px"}}))

;; ### Highcharts

(kind/highcharts
 {:title {:text "Line chart"}
  :subtitle {:text "By Job Category"}
  :yAxis {:title {:text "Number of Employees"}}
  :series [{:name "Installation & Developers"
            :data [43934, 48656, 65165, 81827, 112143, 142383,
                   171533, 165174, 155157, 161454, 154610]}

           {:name "Manufacturing",
            :data [24916, 37941, 29742, 29851, 32490, 30282,
                   38121, 36885, 33726, 34243, 31050]}

           {:name "Sales & Distribution",
            :data [11744, 30000, 16005, 19771, 20185, 24377,
                   32147, 30912, 29243, 29213, 25663]}

           {:name "Operations & Maintenance",
            :data [nil, nil, nil, nil, nil, nil, nil,
                   nil, 11164, 11218, 10077]}

           {:name "Other",
            :data [21908, 5548, 8105, 11248, 8989, 11816, 18274,
                   17300, 13053, 11906, 10073]}]

  :xAxis {:accessibility {:rangeDescription "Range: 2010 to 2020"}}

  :legend {:layout "vertical",
           :align "right",
           :verticalAlign "middle"}

  :plotOptions {:series {:label {:connectorAllowed false},
                         :pointStart 2010}}

  :responsive {:rules [{:condition {:maxWidth 500},
                        :chartOptions {:legend {:layout "horizontal",
                                                :align "center",
                                                :verticalAlign "bottom"}}}]}})

;; ### Observable

;; [Observable](https://observablehq.com/) visualizations are supported
;; when rendering through Quarto.

;; The following is adapted from the [Penguins example](https://quarto.org/docs/interactive/ojs/examples/penguins.html) in Quarto's documentation.

;; Note that you can save your Clojure data as a csv file and refer to it
;; from within your Observable code.
;; See [Referring to files](./#referring-to-files)
;; for more information. In this case, we are using the local file, `"notebooks/datasets/palmer-penguins.csv"`,
;; which is transparently copied by Clay alongside the target HTML.

(kind/observable
 "
//| panel: input
viewof bill_length_min = Inputs.range(
                                      [32, 50],
                                      {value: 35, step: 1, label: 'Bill length (min):'}
                                      )
viewof islands = Inputs.checkbox(
                                 ['Torgersen', 'Biscoe', 'Dream'],
                                 { value: ['Torgersen', 'Biscoe'],
                                  label: 'Islands:'
                                  }
                                 )

Plot.rectY(filtered,
            Plot.binX(
                      {y: 'count'},
                      {x: 'body_mass_g', fill: 'species', thresholds: 20}
                      ))
 .plot({
        facet: {
                data: filtered,
                x: 'sex',
                y: 'species',
                marginRight: 80
                },
        marks: [
                Plot.frame(),
                ]
        }
       )
Inputs.table(filtered)
penguins = FileAttachment('notebooks/datasets/palmer-penguins.csv').csv({ typed: true })
filtered = penguins.filter(function(penguin) {
                                           return bill_length_min < penguin.bill_length_mm &&
                                           islands.includes(penguin.island);
                                           })
")


;; More examples from [Quarto's Observable documentation](https://quarto.org/docs/interactive/ojs/):


(kind/observable
 "athletes = FileAttachment('notebooks/datasets/athletes.csv').csv({typed: true})")

(kind/observable
 "athletes")

(kind/observable
 "Inputs.table(athletes)")

(kind/observable
 "
Plot.plot({
  grid: true,
  facet: {
    data: athletes,
    y: 'sex'
  },
  marks: [
    Plot.rectY(
      athletes,
      Plot.binX({y: 'count'}, {x: 'weight', fill: 'sex'})
    ),
    Plot.ruleY([0])
  ]
})
")

(kind/observable
 "population = FileAttachment('notebooks/datasets/population.json').json()")

(kind/observable
 "population")

(kind/observable
 " import { chart } with { population as data } from '@d3/zoomable-sunburst'
 chart")



;; ### Leaflet

;; This example was adapted from [the Leaflet website](https://leafletjs.com/). Note we are defining a tile layer using [leaflet-providers](https://github.com/leaflet-extras/leaflet-providers).

(kind/reagent
 ['(fn []
     [:div {:style {:height "200px"}
            :ref (fn [el]
                   (let [m (-> js/L
                               (.map el)
                               (.setView (clj->js [51.505 -0.09])
                                         13))]
                     (-> js/L
                         .-tileLayer
                         (.provider "OpenStreetMap.Mapnik")
                         (.addTo m))
                     (-> js/L
                         (.marker (clj->js [51.5 -0.09]))
                         (.addTo m)
                         (.bindPopup "A pretty CSS popup.<br> Easily customizable.")
                         (.openPopup))))}])]
 ;; Note we need to mention the dependency:
 {:html/deps [:leaflet]})

;; ### D3
;; The following example is adapted from [hiccup-d3](https://rollacaster.github.io/hiccup-d3/).
;; The code is a bit different, e.g. `(.scaleOrdinal js/d3 (.-schemeCategory10 js/d3))`
;; instead of `(d3/scaleOrdinal d3/schemeCategory10)`.
;; We still need to figure out how to make hiccup-d3's examples work as they are.

(let [letter-frequencies [{:letter "A", :frequency 0.08167}
                          {:letter "B", :frequency 0.01492}
                          {:letter "C", :frequency 0.02782}
                          {:letter "D", :frequency 0.04253}
                          {:letter "E", :frequency 0.12702}
                          {:letter "F", :frequency 0.02288}
                          {:letter "G", :frequency 0.02015}
                          {:letter "H", :frequency 0.06094}
                          {:letter "I", :frequency 0.06966}
                          {:letter "J", :frequency 0.00153}
                          {:letter "K", :frequency 0.00772}
                          {:letter "L", :frequency 0.04025}
                          {:letter "M", :frequency 0.02406}
                          {:letter "N", :frequency 0.06749}
                          {:letter "O", :frequency 0.07507}
                          {:letter "P", :frequency 0.01929}
                          {:letter "Q", :frequency 0.00095}
                          {:letter "R", :frequency 0.05987}
                          {:letter "S", :frequency 0.06327}
                          {:letter "T", :frequency 0.09056}
                          {:letter "U", :frequency 0.02758}
                          {:letter "V", :frequency 0.00978}
                          {:letter "W", :frequency 0.0236}
                          {:letter "X", :frequency 0.0015}
                          {:letter "Y", :frequency 0.01974}
                          {:letter "Z", :frequency 0.00074}]]
  (kind/reagent
   ['(fn [data]
       (let [size 400
             x (-> js/d3
                   .scaleLinear
                   (.range (into-array [0 size]))
                   (.domain (into-array [0 (apply max (map :frequency data))])))
             y (-> js/d3
                   .scaleBand
                   (.domain (into-array (map :letter data)))
                   (.range (into-array [0 size])))
             color (.scaleOrdinal js/d3 (.-schemeCategory10 js/d3))]
         [:svg
          {:viewBox (str "0 0 " size " " size)}
          (map
           (fn
             [{:keys [letter frequency]}]
             [:g
              {:key letter, :transform (str "translate(" 0 "," (y letter) ")")}
              [:rect
               {:x (x 0),
                :height (.bandwidth y),
                :fill (color letter),
                :width (x frequency)}]])
           data)]))
    letter-frequencies]
   {:html/deps [:d3]}))

;; ### ggplotly
;; (WIP)

;; Clay supports rendering plots through the JS client side of [ggplotly](https://plotly.com/ggplot2/)
;; - an R package offering a Plotly fronted for ggplot2's grammar of graphics implementation.
;; This package is part of the [htmlwidgets](https://www.htmlwidgets.org/) ecosystem,
;; and we represent that in the kind's name.

;; The following is a work-in-progress attempt to generate
;; JSON specs of the kind consumed by ggplotly's client side.

;; The following spec function was generaged by mimicking R's
;; `ggplotly(ggplot(mtcars, aes(wt, mpg)) + geom_point())`.
;; Therefore, some parts are hard-coded and require generalization.
;; Other parts are missing (e.g., specifying colours).

(defn ->ggplotly-spec [{:keys [layers labels]}]
  (kind/htmlwidgets-ggplotly
   (let [;; assuming a single layer for now:
         {:keys [data xmin xmax ymin ymax]} (first layers)
         ;; an auxiliary function to compute tick values:
         ->tickvals (fn [l r]
                      (let [jump (-> (- r l)
                                     (/ 6)
                                     math/floor
                                     int
                                     (max 1))]
                        (-> l
                            math/ceil
                            (range r jump))))]
     {:x
      {:config
       {:doubleClick "reset",
        :modeBarButtonsToAdd ["hoverclosest" "hovercompare"],
        :showSendToCloud false},
       :layout
       {:plot_bgcolor "rgba(235,235,235,1)",
        :paper_bgcolor "rgba(255,255,255,1)",
        :legend
        {:bgcolor "rgba(255,255,255,1)",
         :bordercolor "transparent",
         :borderwidth 1.88976377952756,
         :font {:color "rgba(0,0,0,1)", :family "", :size 11.689497716895}},
        :xaxis (let [tickvals (->tickvals xmin xmax)
                     ticktext (mapv str tickvals)
                     range-len (- xmax xmin)
                     range-expansion (* 0.1 range-len)
                     expanded-range [(- xmin range-expansion)
                                     (+ xmax range-expansion)]]
                 {:linewidth 0,
                  :nticks nil,
                  :linecolor nil,
                  :ticklen 3.65296803652968,
                  :tickcolor "rgba(51,51,51,1)",
                  :tickmode "array",
                  :gridcolor "rgba(255,255,255,1)",
                  :automargin true,
                  :type "linear",
                  :tickvals tickvals
                  :zeroline false,
                  :title
                  {:text (:x labels),
                   :font {:color "rgba(0,0,0,1)", :family "", :size 14.6118721461187}},
                  :tickfont {:color "rgba(77,77,77,1)", :family "", :size 11.689497716895},
                  :autorange false,
                  :showticklabels true,
                  :showline false,
                  :showgrid true,
                  :ticktext ticktext
                  :ticks "outside",
                  :gridwidth 0.66417600664176,
                  :anchor "y",
                  :domain [0 1],
                  :hoverformat ".2f",
                  :tickangle 0,
                  :tickwidth 0.66417600664176,
                  :categoryarray ticktext,
                  :categoryorder "array",
                  :range expanded-range},)
        :font {:color "rgba(0,0,0,1)", :family "", :size 14.6118721461187},
        :showlegend false,
        :barmode "relative",
        :yaxis (let [tickvals (->tickvals ymin ymax)
                     ticktext (mapv str tickvals)
                     range-len (- ymax ymin)
                     range-expansion (* 0.1 range-len)
                     expanded-range [(- ymin range-expansion)
                                     (+ ymax range-expansion)]]
                 {:linewidth 0,
                  :nticks nil,
                  :linecolor nil,
                  :ticklen 3.65296803652968,
                  :tickcolor "rgba(51,51,51,1)",
                  :tickmode "array",
                  :gridcolor "rgba(255,255,255,1)",
                  :automargin true,
                  :type "linear",
                  :tickvals tickvals,
                  :zeroline false,
                  :title
                  {:text (:y labels),
                   :font {:color "rgba(0,0,0,1)", :family "", :size 14.6118721461187}},
                  :tickfont {:color "rgba(77,77,77,1)", :family "", :size 11.689497716895},
                  :autorange false,
                  :showticklabels true,
                  :showline false,
                  :showgrid true,
                  :ticktext ticktext,
                  :ticks "outside",
                  :gridwidth 0.66417600664176,
                  :anchor "x",
                  :domain [0 1],
                  :hoverformat ".2f",
                  :tickangle 0,
                  :tickwidth 0.66417600664176,
                  :categoryarray ticktext,
                  :categoryorder "array",
                  :range expanded-range},)
        :hovermode "closest",
        :margin
        {:t 25.7412480974125,
         :r 7.30593607305936,
         :b 39.6955859969559,
         :l 37.2602739726027},
        :shapes
        [{:yref "paper",
          :fillcolor nil,
          :xref "paper",
          :y1 1,
          :type "rect",
          :line {:color nil, :width 0, :linetype []},
          :y0 0,
          :x1 1,
          :x0 0}]},
       :highlight
       {:on "plotly_click",
        :persistent false,
        :dynamic false,
        :selectize false,
        :opacityDim 0.2,
        :selected {:opacity 1},
        :debounce 0},
       :base_url "https://plot.ly",
       :cur_data "1f2fea5b54d146",
       :source "A",
       :shinyEvents
       ["plotly_hover"
        "plotly_click"
        "plotly_selected"
        "plotly_relayout"
        "plotly_brushed"
        "plotly_brushing"
        "plotly_clickannotation"
        "plotly_doubleclick"
        "plotly_deselect"
        "plotly_afterplot"
        "plotly_sunburstclick"],
       :attrs {:1f2fea5b54d146 {:x {}, :y {}, :type "scatter"}},
       :data
       [{:y (:y data)
         :hoveron "points",
         :frame nil,
         :hoverinfo "text",
         :marker
         {:autocolorscale false,
          :color "rgba(0,0,0,1)",
          :opacity 1,
          :size 5.66929133858268,
          :symbol "circle",
          :line {:width 1.88976377952756, :color "rgba(0,0,0,1)"}},
         :mode "markers"
         :type "scatter",
         :xaxis "x",
         :showlegend false,
         :yaxis "y",
         :x (:x data)
         :text (-> data
                   (tc/select-columns [:x :y])
                   (tc/rows :as-maps)
                   (->> (mapv pr-str)))}]},
      :evals [],
      :jsHooks []})))

(require '[tech.v3.datatype.functional :as fun])

;; A random walk example:
(let [n 100
      xs (range n)
      ys (reductions + (repeatedly n #(- (rand) 0.5)))
      xmin (fun/reduce-min xs)
      xmax (fun/reduce-max xs)
      ymin (fun/reduce-min ys)
      ymax (fun/reduce-max ys)
      data (tc/dataset {:x xs
                        :y ys})]
  (->ggplotly-spec
   {:layers [{:data data
              :xmin xmin :xmax xmax
              :ymin ymin :ymax ymax}]
    :labels {:x "wt"
             :y "mpg"}}))

;; ### 3DMol.js

;; Embedding a 3Dmol Viewer ([original example](https://3dmol.csb.pitt.edu/doc/tutorial-embeddable.html)):

(kind/reagent
 ['(fn [{:keys [data-pdb]}]
     [:div {:style {:height "400px"
                    :width "400px"
                    :position :relative}
            :class "viewer_3Dmoljs"
            :data-pdb data-pdb
            :data-backgroundcolor "0xffffff"
            :data-style "stick"
            :data-ui true}])
  {:data-pdb "2POR"}]
 ;; Note we need to mention the dependency:
 {:html/deps [:three-d-mol]})

;; Using 3Dmol within your code (inspired by [these examples](https://3dmol.csb.pitt.edu/doc/tutorial-code.html)):

(defonce pdb-2POR
(slurp "https://files.rcsb.org/download/2POR.pdb"))

(kind/reagent
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
 {:pdb-data pdb-2POR}]
;; Note we need to mention the dependency:
{:html/deps [:three-d-mol]})

;; ### Video
(kind/video {:youtube-id "DAQnvAgBma8"})

(kind/video {:youtube-id "DAQnvAgBma8"
             :allowfullscreen false})

(kind/video {:youtube-id "DAQnvAgBma8"
             :iframe-width 480
             :iframe-height 270})

(kind/video {:youtube-id "DAQnvAgBma8"
             :embed-options {:mute 1
                             :controls 0}})

;; See, e.g.,  [HTML Youtube Videos](https://www.w3schools.com/html/html_youtube.asp) on w3schools.

;; ### Embedded Portal

;; We may embed [Portal](https://github.com/djblue/portal)'s data-navigating viewers using `kind/portal`.
(kind/portal {:x (range 3)})

;; Note that `kind/portal` applies the [kind-portal](https://github.com/scicloj/kind-portal) adapter to nested kinds.
(kind/portal
[(kind/hiccup [:img {:height 50 :width 50
                     :src "https://clojure.org/images/clojure-logo-120b.png"}])
 (kind/hiccup [:img {:height 50 :width 50
                     :src "https://raw.githubusercontent.com/djblue/portal/fbc54632adc06c6e94a3d059c858419f0063d1cf/resources/splash.svg"}])])

(kind/portal
[(kind/hiccup [:big [:big "a plot"]])
 (random-vega-lite-plot 9)])

;; ### Nesting kinds in Hiccup

;; Kinds are treated recursively inside Hiccup:

(kind/hiccup
 [:div {:style {:background "#f5f3ff"
                :border "solid"}}

  [:hr]
  [:pre [:code "kind/md"]]
  (kind/md "*some text* **some more text**")

  [:hr]
  [:pre [:code "kind/code"]]
  (kind/code "{:x (1 2 [3 4])}")

  [:hr]
  [:pre [:code "kind/dataset"]]
  (tc/dataset {:x (range 33)
               :y (map inc (range 33))})

  [:hr]
  [:pre [:code "kind/table"]]
  (kind/table
   (tc/dataset {:x (range 33)
                :y (map inc (range 33))}))

  [:hr]
  [:pre [:code "kind/vega-lite"]]
  (random-vega-lite-plot 9)

  [:hr]
  [:pre [:code "kind/vega-lite"]]
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
  [:pre [:code "kind/reagent"]]
  (kind/reagent
   ['(fn [numbers]
       [:p {:style {:background "#d4ebe9"}}
        (pr-str (map inc numbers))])
    (vec (range 40))])])


;; ### Nesting kinds in Tables

;; Kinds are treated recursively inside Tables:

(kind/table
{:column-names [(kind/hiccup
                 [:div {:style {:background-color "#ccaabb"}} [:big ":x"]])
                (kind/hiccup
                 [:div {:style {:background-color "#aabbcc"}} [:big ":y"]])]
 :row-vectors [[(kind/md "*some text* **some more text**")
                (kind/code "{:x (1 2 [3 4])}")]
               [(tc/dataset {:x (range 3)
                             :y (map inc (range 3))})
                (random-vega-lite-plot 9)]
               [(kind/hiccup [:div.clay-limit-image-width
                              clay-image])
                (kind/md "$x^2$")]]})

(kind/table
{:column-names ["size" "square"]
 :row-vectors (for [i (range 20)]
                (let [size (* i 10)
                      px (str size "px")]
                  [size
                   (kind/hiccup
                    [:div {:style {:height px
                                   :width px
                                   :background-color "purple"}}])]))}
{:use-datatables true})

;; ### More nesting examples

{:plot (random-vega-lite-plot 9)
 :dataset (tc/dataset {:x (range 3)
                       :y (repeatedly 3 rand)})}

[(random-vega-lite-plot 9)
 (tc/dataset {:x (range 3)
              :y (repeatedly 3 rand)})]

;; ## Fragments

;; `kind/fragment` is a special kind. It expects a sequential value and generates multiple items, of potentially multiple kinds, from its elements.

(->> ["purple" "darkgreen" "brown"]
     (mapcat (fn [color]
               [(kind/md (str "### subsection: " color))
                (kind/hiccup [:div {:style {:background-color color
                                            :color "lightgrey"}}
                              [:big [:p color]]])]))
     kind/fragment)

(->> (range 3)
     kind/fragment)

;; Importantly, markdown subsections affect the Quarto table of contents.

;; ## Functions

;; `kind/fn` is a special kind. It is displayed by first evaluating
;; the given function and arguments, then proceeding recursively
;; with the resulting value.

(kind/fn
  [+ 1 2])

(kind/fn
  [tc/dataset
   {:x (range 3)
    :y (repeatedly 3 rand)}])

;; ## Delays

;; Clojure Delays are a common way to define computations that do not take place immediately. The computation takes place when dereferencing the value for the first time.

;; Clay makes sure to dererence Delays when passing values for visualization.

;; This is handy for slow example snippets and explorations, that one would typically not like to slow down the evaluation of the whole namespace, but would like to visualize them on demand and also include in them in the final document.

(delay
  (Thread/sleep 500)
  (+ 1 2))

;; ## Referring to files

;; In data visualizations, one can directly refrer to files places under `"notebooks/"` or `"src/"`. By default, all files except of these directories, except for Clojure files, are copied alongside the HTML target.
;;
;; This default can be overridden using the `:subdirs-to-sync` config option. E.g., `:subdirs-to-sync ["notebooks" "data"]` will copy files from the `"notebooks"` and `"data"` directories, but not from `"src"`.

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

;; ## Hiding code

;; By default, a Clay notebook shows both the code and the result of an evaluated form.
;; Here are a few ways one may hide the code:
;;
;; 1. Add the metadata `:kindly/hide-code true` to the form (e.g., by preceding it with `^:kindly/hide-code`).
;; 2. Add the metadata `:kindly/hide-code true` to the value.
;; 3. Globally define certain kinds (e.g., `:kind/md`, `:kind/hiccup`) to always hide code (on project level or namespace level) by adding thme as a set to the project config or namespace config, e.g., `:kindly/options {:kinds-that-hide-code #{:kind/md :kind/hiccup}}`.

;; ## Test generation

;; (experimental 🛠)

(+ 1 2)

;; Let us generate tests checking whether
;; this last value is greater than 2.9.
;; We can do it in a few ways.

(kind/test-last [> 2.9])

^kind/test-last
[> 2.9]

(kindly/check > 2.9)

;; We include the test annotations in the markdown text,
;; since the annotations themselves are invisible.

(kind/code
 "(kind/test-last [> 2.9])

^kind/test-last
[> 2.9]

(kindly/check > 2.9)")

;; See the generated [test/index_generated_test.clj](https://github.com/scicloj/clay/blob/main/test/index_generated_test.clj).
