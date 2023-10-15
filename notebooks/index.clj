;; .
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

;; [Clay](https://github.com/scicloj/clay) is a minimalistic Clojure tool for data visualization and literate programming, compatible with the [Kindly](https://scicloj.github.io/kindly/) convention.
;;
;; Also see the [Babashka Conf 2023 talk](https://www.youtube.com/watch?v=HvhMsv3iVGM).

;; ### Goals

;; - Easily explore & share things for others to easily pick & use.
;; - Encourage writing Kindly-compatible notes for future compatiblity with other tools.
;; - Flow with the REPL: encourage user interactions that flow naturally with the typical use of Clojure in editors and REPLs.

;; ## Setup

;; See [the example project](https://github.com/scicloj/clay/tree/main/examples/example-project) for a concrete example.

;; To enjoy Clay's dynamic interaction, you also need to inform it about code evaluations. This requires some setup at the your editor.
;;
;; See the suggested setup for popular editors below. If your favourite editor is not supported yet, let us talk and make it work.

;; ### VSCode Calva
;; **(to be updated soon)**
;;
;; Please add the following command to your [`keybindings.json` file](https://code.visualstudio.com/docs/getstarted/keybindings#_advanced-customization) at the VScode setup (you may pick another key, of course). This command would evaluate a piece of code and send the result to be visualized in Clay.

;; ```json
;; {
;;  "key": "ctrl+shift+enter",
;;  "command": "calva.runCustomREPLCommand",
;;  "args": "(scicloj.clay.v2.api/handle-form! (quote $current-form))"
;;  }
;; ```

;; ### Emacs CIDER
;; **(to be updated soon)**
;;
;; Please load [clay.el](https://github.com/scicloj/clay/blob/main/clay.el) at your Emacs config.
;;
;; It offers the following functions, that you may wish to create keybindings for:
;;
;; |name|function|
;; |--|--|
;; |`clay/start`|start clay if not started yet|
;; |`clay/show-namespace`|save clj buffer and render it in the browser view|
;; |`clay/show-namespace-and-write-html`|save clj buffer, render it in the browser view, and save the result as html|
;; |`clay/write-quarto`|save clj buffer, and generate a Quarto mardkown document|
;; |`clay/render-quarto`|save clj buffer, generate a Quarto markdown document, render it as HTML, and show it in the browser view|
;; |`clay/send`|send a single clj form to be rendered in the browser view|
;; |`clay/send-last-sexp`|send the last s-expression|
;; |`clay/send-defun-at-point`|send the [defun-at-point](https://www.emacswiki.org/emacs/ThingAtPoint)|

;; ### IntelliJ Cursive
;;
;; Under preferences, search for "REPL Commands"
;; (or use the menu IntelliJ -> Preferences -> Languages and Frameworks -> Clojure -> REPL Commands)
;;
;; Then add a global command, and edit it with these settings:
;;
;; |  |  |
;; |--|--|
;; | Name: | Send top-level to Clay |
;; | Before Execution: | "Do nothing" |
;; | Execution: | Command `(scicloj.clay.v2.api/handle-form! (quote ~top-level-form))` |
;; | Echo to REPL: | Executed form |
;; | Execution namespace: | Current REPL namespace |
;;
;; It is useful to add 3 commands:
;;
;; * `(scicloj.clay.v2.api/handle-form! (quote ~top-level-form))`
;; * `(scicloj.clay.v2.api/handle-form! (quote ~form-before-caret))`
;; * `(scicloj.clay.v2.api/show-namespace-and-write-html! "~file-path")`
;;
;; You can then add keybindings under Preferences -> Keymap for the new commands.
;;
;; See the Cursive documentation on [REPL commands and substitutions](https://cursive-ide.com/userguide/repl.html#repl-commands) for more details.
;;

;; ## Starting a Clay namespace

;; Now, we can write a namespace and play with Clay.

(ns index
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.kindly.v4.kind :as kind]))

(defonce memoized-slurp
  (memoize slurp))

;; Let us set up Clay.

(clay/swap-options!
 assoc
 :remote-repo {:git-url "https://github.com/scicloj/clay"
               :branch "main"}
 :quarto {:format {:html {:toc true
                          :theme :spacelab}}
          :highlight-style :solarized
          :code-block-background true
          :embed-resources false
          :execute {:freeze true}})

;; These initializations can also be done in a `user.clj` file, making them available for all namespaces in the project.

;; The browser view should open automatically.

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

;; ### Markdown

;; Markdown text (a vector of strings) can be handled using a kind too.

(kind/md
 ["
* This is [markdown](https://www.markdownguide.org/).
  * *Isn't it??*"
  "
* Here is **some more** markdown."])

;; As we can see, this kind is displayed by converting Hiccup to HTML.

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
 [(-> [:img {:height 50 :width 50
             :src "https://clojure.org/images/clojure-logo-120b.png"}]
      kind/hiccup)
  (-> [:img {:height 50 :width 50
             :src "https://raw.githubusercontent.com/djblue/portal/fbc54632adc06c6e94a3d059c858419f0063d1cf/resources/splash.svg"}]
      kind/hiccup)])

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
  [:h3 [:code "kind/vega"]]
  (random-vega-lite-plot 9)


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

;; # Coming soon

;; In the past, Clay used to support various data visualization libraries such as ECharts, Leaflet, 3DMol, MathBox, and KaTeX.
;;
;; These have been disabled in a recent refactoring (Oct. 2023) and will be brought back soon.
