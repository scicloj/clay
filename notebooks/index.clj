;; # Clay

^:kindly/hide-code
(ns index
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [scicloj.kindly.v4.kind :as kind]
            [tablecloth.api :as tc]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.clay.v2.api :as clay]))

^{:kindly/hide-code true
  :kindly/kind :kind/hiccup}
[:img
 {:style {:width "100px"}
  :src "https://raw.githubusercontent.com/scicloj/clay/main/resources/Clay.svg.png"
  :alt "Clay logo"}]

;; ## About

;; [Clay](https://github.com/scicloj/clay) is a minimalistic Clojure tool for data visualization and literate programming, compatible with the [Kindly](https://scicloj.github.io/kindly-noted/kindly) convention.
;; It allows to conduct visual data explorations and create documents (HTML pages like this one, books, blog posts, reports, slideshows) from Clojure source code and comments.
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

;; Try it out by starting a Clojure command line
;; ```bash
;; clj -Sdeps "{:deps {org.scicloj/clay {:mvn/version \"2-beta23\"}}}"
;; ```
;; The `:mvn/version` may be changing frequently, copy the up-to-date version from
;; [![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/clay.svg)](https://clojars.org/org.scicloj/clay).

;; Wait for a while, it will drop you at a prompt reading `user=> `,
;; now let's require the clay namespace by typing
;; ```clj
;; (require '[scicloj.clay.v2.api :as clay])
;; ```
;; and then type:
;; ```clj
;; (clay/make! {:single-form '(+ 1 2 3)})
;; ```
;;
;; The terminal now looks something like below:
;; ```clj
;; $ clj -Sdeps '{:deps {org.scicloj/clay {:mvn/version "2-beta23"}}}'
;; Downloading: org/scicloj/clay/2-beta23/clay-2-beta23.pom from clojars
;; Downloading: org/scicloj/clay/2-beta23/clay-2-beta23.jar from clojars
;; Clojure 1.10.3
;; user=> (require '[scicloj.clay.v2.api :as clay])
;; nil
;; user=> (clay/make! {:single-form '(+ 1 2 3)})
;; serving Clay at http://localhost:1971/
;; [[[[:wrote "docs/.clay.html"] nil]] nil [:watching-new-files #{}]]
;; ```

;; It will open `http://localhost:1971/` in your web browser
;; (or use another port if 1971 is taken),
;; and congratulations, you've just made your first Clay document!

;; Now you can keep updating the document by trying different forms,
;; like
;; ```clj
;; (clay/make! {:single-form '(str "hello" "world")})
;; ```
;; or whatever is interesting to you.
;; Along the way, the web page will get updated automatically for you!

;; At some point, you might find that you'd better write code in a .clj file.
;; No problem, Clay can also render a document from a Clojure file.
;; Here, we take [notebooks/demo.clj](https://raw.githubusercontent.com/scicloj/clay/refs/heads/main/notebooks/demo.clj) as an example.
;; Click the link and save the file to your computer as, say, `/tmp/demo.clj`,
;; then you can render this Clojure namespace (or file if you prefer) by typing the following in the REPL:
;; ```clj
;; (clay/make! {:source-path "/tmp/demo.clj"})
;; ```

;;
;; As your docs evolve, you may want to add more Clojure files, and manage them as a project.
;; You can organize them as a normal Clojure project with a `deps.edn`,
;; you can browse Clay's own [notebooks/](https://github.com/scicloj/clay/tree/main/notebooks)
;; to get a sense.
;;
;; You can also:

;; - Head over to [Examples](clay_book.examples.html) to see what features it provides and corresponding examples.
;; - See the [API](index.html#api) and [Configuration](index.html#configuration) subsections for more options and variations.
;; - See the [Setup](index.html#setup) section and recent [Videos](index.html#videos) for details about integrating Clay with your editor so you do not need to call `make!` yourself.
;;
;; ## Projects using Clay

;; - [Tablecloth documentation](https://scicloj.github.io/tablecloth/)
;; - [Wolframite documentation](https://scicloj.github.io/wolframite)
;; - [Clay documentation](https://scicloj.github.io/clay/)
;; - [Kindly-noted](https://scicloj.github.io/kindly-noted/) - documenting the ecosystem around Kindly
;; - [Noj documentation](https://scicloj.github.io/noj/)
;; - [Tableplot documentation](https://scicloj.github.io/tableplot/)
;; - [Fastmath 3 documentation](https://generateme.github.io/fastmath/clay)
;; - [Tablemath documentation](https://scicloj.github.io/tablemath/)
;; - [ClojisR documentation](https://scicloj.github.io/clojisr/)
;; - [Clojure Tidy Tuesdays](https://codewithkira.com/clojure-tidy-tuesdays/) data-science explorations
;; - [Clojure Data Tutorials](https://scicloj.github.io/clojure-data-tutorials/)
;; - [Clojure Data Scrapbook](https://scicloj.github.io/clojure-data-scrapbook/)
;; - [LLMs tutorial](https://kpassapk.github.io/llama.clj/llama.html) (in spanish) by Kyle Passarelli
;; - [Statistical Computing in Clojure: Functional Approaches to Unsupervised Learning](https://github.com/adabwana/f24-cs7300-final-project/) by Jaryt Salvo
;; - the [SciNoj Light](https://scicloj.github.io/docs/community/groups/scinoj-light/) conference

;; ## Videos

^{:kindly/hide-code true
  :kindly/kind :kind/hiccup}
(->> [
      ["June 10th 2023"
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
       "GsML75MtNXw"]
      ["Jan. 24th 2025"
       "Noj v2 - getting started - from raw data to a blog post (demonstrating CIDER integration and Quarto publishing)"
       "vnvcKtHHMVQ"]
      ["Mar 7th 2025"
       "Noj in a JAR - setup-free Clojure for beginners"
       "gHwFCOkBb_o"]
      ["Mar 7th 2025"
       "Noj Reload Executable"
       "tDz1x2d65C0"]
      ["Mar 24th 2025"
       "Clojure visual-tools 31 - Workflow Demos 5: Clay overview"
       "WiOUiHsq_dc"]
      ["May 2nd 2025"
       "Clojure for data analysis - getting started with Noj v2, VSCode, Calva, and Clay"
       "B1yPkpyiEEs"]]
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

;; There are two ways to add Clay-related commands to Calva.

;; #### Calva Power Tools

;; The [Calva Power Tools](https://marketplace.visualstudio.com/items?itemName=betterthantomorrow.calva-power-tools)
;; extension can be installed from within VSCode `Install Extensions`.

;; It will provide a few Clay-related commands such as `Clay Make File`.

;; You can find these commands and their default key bindings
;; by searching for `"Clay"` in your VCode Command Palette.

;; #### Calva Custom REPL Commands

;; With Clay in your classpath, [Calva](https://calva.io/) will discover
;; [Custom REPL Commands](https://calva.io/custom-commands/).

^:kindly/hide-code
(-> "calva.exports/config.edn"
    io/resource
    slurp
    edn/read-string
    :customREPLCommandSnippets
    tc/dataset
    (tc/select-columns [:name :key])
    kind/table)

;; To invoke a custom REPL command, press `ctrl+alt+space` followed by the corresponding key.
;; Pressing `ctrl+alt+space` followed by `space` opens a quick-pick list of custom REPL commands to invoke.

;; Clay exports these custom command snippets via [resources/calva.exports/config.edn](https://github.com/scicloj/clay/blob/main/resources/calva.exports/config.edn).

;; If you prefer a different keyboard shortcut, use the command palette to find **Preferences: Open Keyboard Shortcuts (JSON)** and add:

;;```json
;;    {
;;        "key": "alt+x",
;;        "command": "calva.runCustomREPLCommand",
;;        "args": ",",
;;        "when": "calva:connected && calva:keybindingsEnabled"
;;    },
;;    {
;;        "key": "shift+alt+x",
;;        "command": "calva.runCustomREPLCommand",
;;        "args": "n",
;;        "when": "calva:connected && calva:keybindingsEnabled"
;;    },
;; ```

;; Now **alt+x** will **Clay make current form as HTML**,
;; and **shift+alt+x** will **Clay make Namespace as HTML**.
;; The "args" matches the "key" of the custom REPL commands.

;; ### Emacs CIDER

;; See the [clay.el](https://github.com/scicloj/clay.el) package for the relevant interactive functions.

;; ### Neovim Conjure

;; See the [clay.nvim](https://github.com/radovanne/clay.nvim) plugin. 

;; ### IntelliJ Cursive
;;
;; At a Clojure project managed in Cursive, the `.idea/repl-commands.xml` file allows defining custom REPL commands.
;; To enable Clay-related commands, you can copy [Clay's configuration](https://github.com/scicloj/clay/blob/main/.idea/repl-commands.xml)
;; into your `.idea/repl-commands.xml` file.
;;
;; You can also copy the file to your IntelliJ configuration directory to make it global for all projects.
;; For example, in Linux, that directory will be something like `~/.config/JetBrains/IdeaIC2025.1/options`,
;; with the appropriate version of IntelliJ Idea or IntelliJ Idea Community Edition.
;;
;; **Caution:** If you have some existing custom REPL commands, you will have to merge the files manually.
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
   [scicloj.clay.v2.main]
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
                 {:=x :sepal-width
                  :=x2 :sepal-length
                  :=y :petal-width
                  :=y2 :petal-length
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
;; and show it at the browser
;; (opening a browser tab if this is the first
;; time using Clay in the session):
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
;; and do not open a browser tab even if this
;; is the first time using Clay in the session:
(comment
  (clay/make! {:source-path "notebooks/index.clj"
               :browse false}))

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

;; Evaluate and render
;; the namespaces in `"notebooks/slides.clj"` `"notebooks/index.clj"`
;; as HTML
;; and toggle the live reload option:
(comment
  (clay/make! {:source-path ["notebooks/slides.clj"
                             "notebooks/index.clj"]
               :live-reload :toggle}))

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

;; Evaluate and render all files in base-source-path:
(comment
  (clay/make! {:base-source-path "other_notebooks"
               :render true}))

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

;; Render an expression that includes a path to a file.
;; The file is automatically synced, since is under `"notebooks"`,
;; which is one of the directories synced by default (`:subdirs-to-sync`).
;; Note that, by default, the root `"notebooks"` is included in the
;; target file path.

(comment
  (clay/make! {:single-value (kind/hiccup
                              [:img {:src "notebooks/images/Clay.svg.png"}])}))

;; Render an expression that includes a path to a file.
;; The file is automatically synced, since is under `"notebooks"`,
;; which is one of the directories synced by default (`:subdirs-to-sync`).
;; Here, we override the default behaviour, so the root `"notebooks"`
;; is not included in the target file path

(comment
  (clay/make! {:single-value (kind/hiccup
                              [:img {:src "images/Clay.svg.png"}])
               :keep-sync-root false}))

;; Demonstrate that we can use the same source and target paths.

(comment
  (clay/make! {:format [:quarto :html]
               :base-source-path "notebooks"
               :source-path "demo.clj"
               :base-target-path "notebooks"}))

;; Demonstrate that we can use the same source and target paths --
;; another variation with `:keep-sync-root false` --
;; in this case, no syncing will take place,
;; because the relevant files already exist in place.

(comment
  (clay/make! {:format [:quarto :html]
               :base-source-path "notebooks"
               :source-path "demo.clj"
               :base-target-path "notebooks"
               :keep-sync-root false}))

;; Render a notebook in a nested source path.
;; By default, the target path is flattened.
;; E.g., `"temp/notebooks.subdir.another_demo.html"`.

(comment
  (clay/make! {:source-path "notebooks/subdir/another_demo.clj"}))

;; Render a notebook but avoid the flattening default behaviour,
;; so that thee target path is not flattened.
;; E.g., `"temp/notebooks/subdir/another_demo.html"`.

(comment
  (clay/make! {:source-path "notebooks/subdir/another_demo.clj"
               :flatten-targets false}))

;; Demonstrate a combination of `:flatten-targets false`
;; and `:keep-sync-root false`.

(comment
  (clay/make! {:source-path "notebooks/demo.clj"
               :flatten-targets false
               :keep-sync-root false}))

;; Reopen the Clay view in the browser
;; (in case you closed the browser tab previously opened):

(comment
  (clay/browse!))

;; ### Live reload
;; (experimental)

;; Clay can listen to file changes (using [nextjournal/beholder](https://github.com/nextjournal/beholder))
;; and respond with remaking the page.

;; See the example above with `:live-reload true`.

;; One caveat: You may not want to use this if the containing directory of this file
;; has a lot of files and/or sub-directories, as it may take quite a long time (e.g. ~1 minute)
;; for beholder to watch the containing directory for file changes.

;; ### Hiccup output

;; (experimental 🛠)

;; Render a notebook in Hiccup format and return the resulting Hiccup structure:

(comment
  (clay/make-hiccup {:source-path "notebooks/index.clj"}))

;; ## CLI
;; (experimental)

;; You can launch Clay From the command line:

;; ```sh
;; clojure -M -m scicloj.clay.v2.main
;; ```

;; Clay will watch the `notebooks` directory in live-reload mode,
;; as described in the [Live reload section](index.html#live-reload).

;; Add a `:clay` alias to your `deps.edn` to make it easier to launch:

;; ```clojure
;; {:aliases
;;   {:clay {:main-opts ["-m" "scicloj.clay.v2.main"]}}}
;; ```

;; Then you can run it a little more conveniently:

;; ```sh
;; clojure -M:clay
;; ```

;; Files and directories to watch can be passed as arguments.
;; If `.clj` files are passed, they will be immediately rendered as HTML.

;; For example:

;; Immediately render `my-namespace` and watch `notebooks`.
;; ```sh
;; clojure -M -m scicloj.clay.v2.main notebooks/my_namespace.clj
;; ```

;; Watch `notebooks1` and `notebooks2` instead of `notebooks`:
;; ```sh
;; clojure -M -m scicloj.clay.v2.main notebooks1 notebooks2
;; ```

;; The `-r` or `--render` argument cancels the `live-reload` behaviour
;; and can be used for rendering files as a batch task.

;; For example:

;; Immediately render `my-namespace` and exit:
;; ```sh
;; clojure -M -m scicloj.clay.v2.main notebooks/my_namespace.clj -r
;; ```

;; Immediately render all notebooks found in base-source-path and exit:
;; ```sh
;; clojure -M:clay -r
;; ```

;;
;; ## Configuration

;; Calls to the `make!` function are affected by various parameters
;; which collected as one nested map.
;; This map is the result of deep-merging configuration sources:
;;
;; - default: [clay-default.edn](https://github.com/scicloj/clay/blob/main/resources/clay-default.edn) under Clay's resources
;; - user: `clay.edn` in project root
;; - namespace: `:clay` metadata found on the ns form
;; - call: the argument to `make!`
;;
;; Here are some of the parameters worth knowing about:
;;
;; | Key | Purpose | Example |
;; |-----|---------|---------|
;; | `:source-path` | files to render | `["notebooks/index.clj"]` |
;; | `:title` | sets the HTML title that appears in the browser tab bar | `"My Title"` |
;; | `:favicon` | sets a page favicon | `"favicon.ico"` |
;; | `:show` | when true (the default) updates the browser view (starts the HTML server if necessary) | `false` |
;; | `:browse` | when true (the default) opens a new browser tab when the HTML server is started for the first time | `false` |
;; | `:ide` | (experimental) causes `make!` to open a webview instead of browser, use `:browse :browser` to avoid | `:calva` |
;; | `:single-form` | render just one form | `(inc 1)` |
;; | `:format` | output quarto markdown and/or html | `[:quarto :html]` |
;; | `:quarto` | adds configuration for Quarto | `{:highlight-style :solarized}` |
;; | `:base-target-path` | the output directory |  `"temp"` |
;; | `:base-source-path` | where to find `:source-path` | `"notebooks"` |
;; | `:clean-up-target-dir` | delete (!) target directory before repopulating it  | `true` |
;; | `:remote-repo` | linking to source | `{:git-url "https://github.com/scicloj/clay" :branch  "main"}` |
;; | `:hide-info-line` | hiding the source reference at the bottom | `true` |
;; | `:hide-ui-header` | hiding the ui info at the top | `true` |
;; | `:pprint-margin` | result rendering will try to wrap anything going beyond this value | `nil` or `72` |
;; | `:post-process` | post-processing the resulting HTML | `#(str/replace "#3" "4")` |
;; | `:live-reload` | make automatically after its source file is changed | `true` or `:toggle` |
;; | `:flatten-targets` | (experimental) whether to place the output in a subdirectory or not | `false` |
;; | `:subdirs-to-sync` | (experimental) subdirs to copy non-clojure files from | `["static"]` |
;; | `:keep-sync-root` | (experimental) keep the subdir prefix | `false` |
;; | `:render` | (experimental) overrides `:show` `:serve` `:browse` and `:live-reload` to `false` | `true` |
;; | `:aliases | (experimental) a vector of aliases (sub maps in configuration) to merge | `[:markdown]` |
;; | `:config/transform` | (experimental) hook to update config per namespace | `:config/transform my.ns/my-fn` |

;; When working interactively, it is helpful to render to a temporary directory that can be git ignored and discarded.
;; For example: you may set `:base-target-path "temp"` at your `clay.edn` file.
;; When publishing a static page, you may wish to target a `docs` directory by setting `:base-target-path "docs"`
;; in your call to `clay/make!`.
;; Creating a dev namespace is a good way to invoke a different configuration for publishing.

;; Rendering a result is based on `clojure.pprint/pprint` behaviour.
;; By default, it will wrap anything beyond `clojure.pprint/*print-right-margin*` (default: 72) number of chars in the single line.
;; For example `(range 100)` will be rendered as long vertical list of numbers.
;; You can overwrite it by setting `:pprint-margin` option.
;; When set to `nil` there won't be wrapping at all and `(range 100)` will be rendered in one horizontal list of numbers.

;; Aliases let you define reusable config fragments and selectively apply them.
;; Add an `:aliases` map to your config with named configurations,
;; then activate them using `:merge-aliases` (for one-time use) or `:reset-aliases` (to persist across invocations).
;; For example, you might use `:merge-aliases [:markdown]` to generate Quarto-friendly Markdown from the REPL,
;; or `:reset-aliases [:html]` in your IDE to default to full HTML rendering.
;; Alternatively you can `(reset! scicloj.clay.v2.config/*current-aliases aliases)`.
;; Aliases are deeply merged into the base config in order.

;; ### Namespace configuration and front matter

;; Configuration is discovered as metadata under the `:clay` key on the namespace form:

^{:clay {:quarto {:myfrontmatterkey "myfrontmattervalue"}}}
(ns index)

;; * the clay metadata is on the form rather than the var itself.
;; * the `:quarto` configuration will be placed in the front matter for markdown output.
;;
;; See [Quarto front matter docs](https://quarto.org/docs/authoring/front-matter.html).

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

;; ## JavaScript Functions
;; (coming soon)

;; Visualizations are often compiled to JavaScript which can make use of functions.
;; To make this accessible, Regex expressions are treated as JavaScript literals.
;; Clojure has a Regex syntax #"...", making it convenient for inserting code.
;; In this example, the tooltip formatter is an inline JavaScript function:

(kind/echarts
 {:title {:text "Echarts Example"}
  :tooltip {:formatter #"(params) => 'hello: ' + params.name"}
  :legend {:data ["sales"]}
  :xAxis {:data ["Shirts", "Cardigans", "Chiffons",
                 "Pants", "Heels", "Socks"]}
  :yAxis {}
  :series [{:name "sales"
            :type "bar"
            :data [5 20 36
                   10 10 20]}]})

;; ## Delays

;; Clojure Delays are a common way to define computations that do not take place immediately. The computation takes place when dereferencing the value for the first time.

;; Clay makes sure to dererence Delays when passing values for visualization.

;; This is handy for slow example snippets and explorations, that one would typically not like to slow down the evaluation of the whole namespace, but would like to visualize them on demand and also include in them in the final document.

(delay
  (Thread/sleep 500)
  (+ 1 2))

;; ## Referring to files

;; In data visualizations, one can directly refrer to files places under `"notebooks/"` or `"src/"`. By default, all files except of these directories, except for Clojure files, are copied alongside the HTML target.

;; This default can be overridden using the `:subdirs-to-sync` config option. E.g., `:subdirs-to-sync ["notebooks" "data"]` will copy files from the `"notebooks"` and `"data"` directories, but not from `"src"`. Clojure source files (`.clj`, etc.) are not synched.

;; Note that the URLs we use below need to include the root directory from which
;; files are synced, which is `"notebooks"` in these cases.
;; This is because, by default, a file like `"notebooks/images/Clay.svg.png"`
;; will be copied to something like `"target/notebooks/images/Clay.svg.png"
;; in the sync step, assuming `:base-target-path` is `"target"`.

;; To override this behaviour, one may set `:keep-sync-root` to `false`.
;; Then,a file like `"notebooks/images/Clay.svg.png"`
;; will be copied to something like `"target/images/Clay.svg.png"
;; in the sync step, and it will not be necessary to include
;; the `"notebooks"` prefix in the code.

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
