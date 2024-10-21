;; # Clay

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
;; **Artifact:** [![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/clay.svg)](https://clojars.org/org.scicloj/clay)
;;
;; **Status:** The project has moved into Beta stage (March 2024).
;;
;;
;; ## Goals

;; - Easily explore & share data visualizations and notebooks for others to easily pick & use.
;; - Encourage writing Kindly-compatible notes for future compatiblity with other tools.
;; - Flow with the REPL: encourage user interactions that flow naturally with the typical use of Clojure in editors and REPLs.

;; ## Getting started

;; Add Clay to your project dependencies:
;; [![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/clay.svg)](https://clojars.org/org.scicloj/clay)

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
;; - [Fastmath 3 documentation](https://generateme.github.io/fastmath/clay)
;; - [ClojisR documentation](https://scicloj.github.io/clojisr/)
;; - [Wolframite documentation](https://scicloj.github.io/wolframite)
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

;; To enjoy Clay's dynamic interaction, you also need to inform it about code evaluations.
;; This requires some editor setup.
;;
;; To use [Quarto](https://quarto.org/)-related actions,
;; it is necessary to have the Quarto CLI [installed](https://quarto.org/docs/get-started/) in your system.
;;
;; See the suggested setup for popular editors below.
;; If your favourite editor is not supported yet, let us talk and make it work.

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

;; See the [clay.el](https://github.com/scicloj/clay.el) package for the relevant interactive functions.

;; ### Neovim Conjure

;; See [Integrating with Clay and data visualisation tools](https://github.com/Olical/conjure/wiki/Integrating-with-Clay-and-data-visualisation-tools) at the Conjure Wiki.

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
  (:require
   [scicloj.kindly.v4.api :as kindly]
   [scicloj.kindly.v4.kind :as kind]
   [scicloj.clay.v2.quarto.highlight-styles :as quarto.highlight-styles]
   [scicloj.clay.v2.quarto.themes :as quarto.themes]
   [scicloj.metamorph.ml.toydata :as toydata]
   [scicloj.tableplot.v1.hanami :as hanami]
   [tablecloth.api :as tc]
   [clojure.string :as str]))

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

;; A plot using [Tableplot](https://github.com/scicloj/tableplot):
(-> (toydata/iris-ds)
    (hanami/plot hanami/rule-chart
                 {:=x :sepal_width
                  :=x2 :sepal_length
                  :=y :petal_width
                  :=y2 :petal_length
                  :=color :species
                  :=color-type :nominal
                  :=mark-size 3
                  :=mark-opacity 0.2}))

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

;; Do the same as above by default
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
;; the namespace in `"notebooks/index.clj"`
;; and use the favicon at `"notebooks/favicon.ico"`
(comment
  (clay/make! {:source-path "notebooks/index.clj"
               :favicon "notebooks/favicon.ico"}))

;; Evaluate and render
;; the namespaces in `"notebooks/slides.clj"` `"notebooks/index.clj"`
;; as HTML
;; and do not show it at the browser:
(comment
  (clay/make! {:source-path ["notebooks/slides.clj"
                             "notebooks/index.clj"]
               :show false}))

;; Evaluate and render
;; the namespaces in `"notebooks/slides.clj"` `"notebooks/index.clj"`
;; as HTML
;; and start watching these files for live reload:
;; (experimental)
(comment
  (clay/make! {:source-path ["notebooks/slides.clj"
                             "notebooks/index.clj"]
               :live-reload true}))


;; Evaluate and render a single form
;; in the context of the namespace in `"notebooks/index.clj"`
;; as HTML
;; and show it at the browser:
(comment
  (clay/make! {:source-path "notebooks/index.clj"
               :single-form '(+ 1 2)}))

;; Evaluate and render a single form
;; in the context of the current namespace (`*ns*`)
;; as HTML
;; and show it at the browser:
(comment
  (clay/make! {:single-form '(+ 1 2)}))

;; Render a single value
;; as HTML
;; and show it at the browser:
(comment
  (clay/make! {:single-value 3}))

;; Render a single value
;; as HTML
;; and process the resulting HTML
;; using a custom function.
(comment
  (clay/make! {:single-value 3333
               :post-process (fn [html]
                               (-> html
                                   (str/replace #"3333" "4444")))}))

;; Render a namespace
;; as HTML
;; and hide the UI banner in the browser view.
(comment
  (clay/make! {:source-path "notebooks/index.clj"
               :hide-ui-header true}))

;; Render a namespace
;; as HTML
;; and hide the information line at the bottom of the page.
(comment
  (clay/make! {:source-path "notebooks/index.clj"
               :hide-info-line true}))

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
;; (note the current browser view of this format
;; it not so sophisticated and lacks live-reload
;; on page updates).
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
;; with a specified favicon:
(comment
  (clay/make! {:format [:quarto :html]
               :base-source-path "notebooks"
               :source-path ["index.clj"
                             "chapter.clj"
                             "another_chapter.md"]
               :base-target-path "book"
               :book {:title "Book Example"
                      :favicon "notebooks/favicon.ico"}
               ;; Empty the target directory first:
               :clean-up-target-dir true}))

;; Create a Quarto book
;; with [book parts](https://quarto.org/docs/books/book-structure.html#parts-appendices):
(comment
  (clay/make! {:format [:quarto :html]
               :base-source-path "notebooks"
               :source-path [{:part "Part A"
                              :chapters ["index.clj"
                                         "chapter.clj"]}
                             {:part "Part B"
                              :chapters ["another_chapter.md"]}]
               :base-target-path "book"
               :book {:title "Book Example"}
               ;; Empty the target directory first:
               :clean-up-target-dir true}))

;; Reopen the Clay view in the browser
;; (in case you closed the browser tab previously opened):

(comment
  (clay/browse!))

;; ### Live reload
;; (experimental)

;; Clay can listen to file changes (using [nextjournal/beholder](https://github.com/nextjournal/beholder))
;; and respond with remaking the page.

;; See the example above with `:live-reload true`.

;; ### Hiccup output

;; (experimental 🛠)

;; Render a notebook in Hiccup format and return the resulting Hiccup structure:

(comment
  (clay/make-hiccup {:source-path "notebooks/index.clj"}))

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
;; | Key | Purpose | Example |
;; |-----|---------|---------|
;; | `:source-path` | files to render | `["notebooks/index.clj"]` |
;; | `:title` | sets the HTML title that appears in the browser tab bar | `"My Title"` |
;; | `:favicon` | sets a page favicon | `"favicon.ico"` |
;; | `:show` | starts the HTML server when true (the default) | `false` |
;; | `:single-form` | render just one form | `(inc 1)` |
;; | `:format` | output quarto markdown and/or html | `[:quarto :html]` |
;; | `:quarto` | adds configuration for Quarto | `{:highlight-style :solarized}` |
;; | `:base-target-path` | the output directory |  `"temp"` |
;; | `:base-source-path` | where to find `:source-path` | `"notebooks"` |
;; | `:clean-up-target-dir` | delete (!) target directory before repopulating it  | `true` |
;; | `:remote-repo` | linking to source | `{:git-url "https://github.com/scicloj/clay" :branch  "main"}` |
;; | `:hide-info-line` | hiding the source reference at the bottom | `true` |
;; | `:hide-ui-header` | hiding the ui info at the top | `true` |
;; | `:post-process` | post-processing the resulting HTML | `#(str/replace "#3" "4")` |
;; | `:live-reload` | whether to make and live reload the HTML automatically after its source file is changed | `true` |

;; When working interactively, it is helpful to render to a temporary directory that can be git ignored and discarded.
;; For example: you may set `:base-target-path "temp"` at your `clay.edn` file.
;; When publishing a static page, you may wish to target a `docs` directory by setting `:base-target-path "docs"`
;; in your call to `clay/make!`.
;; Creating a dev namespace is a good way to invoke a different configuration for publishing.

;; ## Kinds

;; The way things should be visualized is determined by the
;; [Kindly](https://scicloj.github.io/kindly-noted/kindly)
;; specification.

;; Kindly advises tools (like Clay) about the kind of way a given context
;; should be displayed, by assigning to it a so-called kind.

;; Please refer to the Kindly documentation for details about specifying
;; and using kinds.

;; In this documentation we demonstrate Kindly's default advice.
;; [User-defined](https://scicloj.github.io/kindly-noted/kindly_advice.html#extending) Kindly [advices](https://scicloj.github.io/kindly-noted/kindly_advice.html) should work as well.

;; ## Examples

;; See the dedicated 📖 [Examples chapter](./clay_book.examples.html) 📖 of this book.

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
;; a given function and arguments, then proceeding recursively
;; with the resulting value.

;; The function can be specified through the Kindly options.
(kind/fn {:x 1
          :y 2}
  {:kindly/f (fn [{:keys [x y]}]
               (+ x y))})

(kind/fn {:my-video-src "https://file-examples.com/storage/fe58a1f07d66f447a9512f1/2017/04/file_example_MP4_480_1_5MG.mp4"}
  {:kindly/f (fn [{:keys [my-video-src]}]
               (kind/video
                {:src my-video-src}))})

;; If the value is a vector, the function is the first element, and the arguments are the rest.

(kind/fn
  [+ 1 2])

;; If the value is a map, the function is held at the key `:kindly/f`, and the argument is the map.

(kind/fn
  {:kindly/f (fn [{:keys [x y]}]
               (+ x y))
   :x 1
   :y 2})

;; The kind of the value returned by the function is respected.
;; For example, here are examples with a function returning `kind/dataset`.

(kind/fn
  {:x (range 3)
   :y (repeatedly 3 rand)}
  {:kindly/f tc/dataset})

(kind/fn
  [tc/dataset
   {:x (range 3)
    :y (repeatedly 3 rand)}])

(kind/fn
  {:kindly/f tc/dataset
   :x (range 3)
   :y (repeatedly 3 rand)})

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
;; This default can be overridden using the `:subdirs-to-sync` config option. E.g., `:subdirs-to-sync ["notebooks" "data"]` will copy files from the `"notebooks"` and `"data"` directories, but not from `"src"`. Clojure source files (`.clj`, etc.) are not synched.

(kind/hiccup
 [:img {:src "notebooks/images/Clay.svg.png"}])

(kind/image
 {:src "notebooks/images/Clay.svg.png"})

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
;; 3. Globally define certain kinds (e.g., `:kind/md`, `:kind/hiccup`) to always hide code (on project level or namespace level) by adding theme as a set to the project config or namespace config, e.g., `:kindly/options {:kinds-that-hide-code #{:kind/md :kind/hiccup}}`.

;; ## Test generation

;; (experimental 🛠)

(+ 1 2)

(kind/test-last [> 2.9])

^kind/test-last
[> 2.9]

(kindly/check > 2.9)

;; We generate tests checking whether
;; this last value is greater than 2.9.
;; We can do it in a few ways.

;; We include the test annotations in the markdown text,
;; since the annotations themselves are invisible.

(kindly/hide-code
 (kind/code
  "(kind/test-last [> 2.9])

^kind/test-last
[> 2.9]

(kindly/check > 2.9)"))

;; See the generated [test/index_generated_test.clj](https://github.com/scicloj/clay/blob/main/test/index_generated_test.clj).

;; For a detailed example using this mechanism, see [the source](https://github.com/scicloj/clojisr/blob/master/notebooks/clojisr/v1/tutorials/main.clj) of the [ClojisR tutorial](https://scicloj.github.io/clojisr/clojisr.v1.tutorials.main.html).

;; ## CSS classes and styles
;; ### Styling HTML visualizations

;; Clay will transfer CSS classes and styles present in `:kindly/options` metadata to the visualization.
;; The recommended way to prepare `:kindly/options` metadata is through the `kind` api:

(kind/table {:column-names ["A" "B" "C"]
             :row-vectors  [[1 2 3] [4 5 6]]}
            {:class "table-responsive"
             :style {:background "#f8fff8"}})

;; See also the Kindly documentation on [passing options](https://scicloj.github.io/kindly-noted/kindly#passing-options).
;; Optional class and style attributes will only be applied to hiccup elements (not markdown content).

;; ### Styling Markdown content

;; Quarto uses pandoc attributes (see https://quarto.org/docs/authoring/markdown-basics.html#sec-divs-and-spans) to attach classes.
;; ```
;; ::: {.alert .alert-primary}
;; Example alert
;; :::
;; ```
;; ::: {.alert .alert-primary}
;; Example alert
;; :::
;; | A | B | C |
;; |---|---|---|
;; | 1 | 2 | 3 |
;; | 4 | 5 | 6 |
;; : This table is responsive {.responsive}

;; Markdown styling is not currently handled when rendering direct to HTML.

;; ## Varying kindly options

;; (experimental)

;; `kindly/merge-options!` varies the options to affect the notes coming below.
;; Let us use it to present code and value horizontally.
;; By default, calls to `kindly/merge-options!` are hidden.
;; In this document, we use `#(kindly/hide-code % false)` to make them visible.`

(kindly/hide-code
 (kindly/merge-options! {:code-and-value :horizontal})
 false)

(+ 1 2)

(+ 3 4)

;; Let us change it back.

(kindly/hide-code
 (kindly/merge-options! {:code-and-value :vertical})
 false)

(+ 1 2)

(+ 3 4)

;; Let us now change the background color.

(kindly/hide-code
 (kindly/merge-options! {:style {:background-color "#ccddee"}})
 false)

(kind/hiccup
 [:div
  [:p "hello"]])

;; In Quarto-based rendering, datasets are rendered as plain Markdown,
;; and HTML options are not applied at the moment.

(tc/dataset {:x (range 3)})

;; To make sure the background color is applied, we wrap it with Hiccup.

(kind/hiccup
 [:div
  (tc/dataset {:x (range 3)})])

;; Let us cancel the setting of the background color.

(kindly/hide-code
 (kindly/merge-options! {:style {:background-color nil}})
 false)

(kind/hiccup
 [:div
  [:p "hello"]])
