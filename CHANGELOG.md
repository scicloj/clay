# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [2-beta20] - 2024-10-22
- updated deps: kindly, kindly-advice
- added support for `:kind/emmy-viewers`

## [2-beta19] - 2024-10-22
- a minor cleanup of the data structure being read from code
- bugfix: made the special kinds `:kind/fn` and `:kind/fragment` nest inside others

## [2-beta18] - 2024-10-20
- `:live-reload` support - experimental - thanks, @whatacold
- emmy-viewers support - experimental - thanks, @reedho

## [2-beta17] - 2024-09-30
- removed unused require (PR #162) - thanks, @schneiderlin, @mchughs
- updated `nextjournal/mardkown` version, which brings Java 22 support
- add original line-number to generated test vars names (#163) - thanks, @behrica
- cleaning "\r\n" to avoid a tools.reader bug - thanks, @schneiderlin
- added support for image urls in `:kind/image` - thanks, @lightmatters
- added support for video urls in `:kind/video` - thanks, @holyjak
- added support for `:kindly/options` in `:kind/fn` - thanks, @holyjak

## [2-beta16] - 2024-09-14
- Class or style from kindly/options (#139)
- updated Kindly and Kindly-advice versions
- rely on `*ns*` `:kindly/options` and kindly-advice for options (PR #148)
- bugfix in handling `:kindly/options` at item preparation
- check for horizontal in clay and kindly options (PR #149)
- bugfix in handling options in notebook generation
- bugfix: handling the edge case of string hiccup carefully
- bugfix: handling `:kind/hidden` correctly in notebook generation
- applying `:kindly/options` in all hiccup-based pathways
- bugfix: using the appropriate Quarto target format in Markdown generation (fixes #137, #147)
- removed the `:fontsize` setting in the default config (as it interferred with revealjs slides) (fixes #155)
- added support for hiding the info line; server refactoring (fixes #156)
- added support for hiding the ui header; server refactoring (fixes #154)
- added support for HTML post-processing (`:post-process` option)
- bugfix: fixed the `:inline-js-and-css` support at the experimental `make-hiccup` funciton (fixes #144)


## [2-beta15] - 2024-07-27
- introducing code-and-value and horizontal layout - WIP (PR #127)
- code cleanup (PR #131)
- fix backquote readinging in wrong namespace affecting macros (#132)
- fix switching to the namespace (#92)

## [2-beta14] - 2024-07-22
- proper handling of static resources (PR #126)

## [2-beta13] - 2024-07-19
- fixed server should respond with 404 when file is not found
- serve /favicon.ico and /Clay.svg.png locally from resources
- removed the avoid-favicon code, it is not needed, browsers will now find favicon.ico in development.
- introduced :favicon configuration for adding a favicon in both quarto and html
- added initial configuration table to index
- bugfix: less agressive leading space cleaning (#123) - thanks, @genmeblog

## [2-beta12] - 2024-07-13
- regression fix: make `*stop-server!` a fn for stopping the server - thanks, @olavfosse
- teach `start!` to take a port number (#107) - thanks, @olavfosse
- pass all relevant options to Quarto book setup (#114)
- bugfix: correctly include inline JS - thanks, Jarkko Saltiola
- refactoring of config propagation
- Quarto book parts support 

## [2-beta11] - 2024-06-15
- support for handling any sequential of texts (not just vector) in `kind/code`, `kind/md`, `kind/html` (#103)
- mode careful escaping of characters (#104)
- using kindly-advice earlier in the pipeline, e.g. to recognize kinds where code should be hidden (#105)
- bugfix: the `kind/image` annotation failed to render (#105)
- updated Kindly version
- `kind/tex` support
- updated version for KaTeX js dependency
- supporting TeX inside Markdown in all cases (not just some Quarto cases as before)
- support for inline js and css in page generation (WIP)

## [2-beta10] - 2024-05-25
- added classes to elements that have styling applied to facilitate custom styles (#102)
- added partial support for dataset print options as kindly options
- made a minor change in `make!` return values for clarity
- added support for returning a page as plain Hiccup (experimental)

## [2-beta9] - 2024-05-18
- extended the `kind/fn` semantics to a map spec

## [2-beta8] - 2024-04-22
- allowing `.Rmd` files in Quarto books
- test-generation support - experimental

## [2-beta7] - 2024-04-10
- added `kind/fn` support - evaluating given function and arguments and displaying the result

## [2-beta6] - 2024-04-09
- Calva custom REPL commands - making sure `:base-source-path` is `nil` in all `make!` calls (#96)
- bugfix: using a temporary target HTML in single form evaluations (#93)
- avoiding target directory cleanup on single form evaluations (#97)
- removing redundant merge operation in `make!` implementation (should not change any behaviour)

## [2-beta5] - 2024-04-06
- removed escaping in printed values (probably not needed anymore in current pathways and did create problems with `<` `>` symbols)

## [2-beta4] - 2024-04-05
- updated the `[:html]` target to use the `bootstrap.min.css` currently generated by `[:quarto :html]`
- changed the default styling of plots (WIP)
- changed the default Quarto config - smaller font size

## [2-beta3] - 2024-03-29
- fixed a Clojar deployment problem (broken Github link)

## [2-beta1] - 2024-03-29
- updated deps (no longer using a temporary adaptation of Portal)

## [2-alpha87] - 2024-03-19
- `kind/highcharts` support - PR by @adham-omran

## [2-alpha86] - 2024-03-19
- bugfix: copying the `:htmlwidgets-plotly` deps correctly (avoiding copying the `.git` subdirectory which breaks the doc served on github pages)

## [2-alpha85] - 2024-03-19
- extended `kind/plotly` API - PR by @roterski
- using `:html/deps` to specify dependencies in `:kindly/options` (depracating the confusing `:reagent/deps`, still supported though)
- support for adding deps on `kind/hiccup`, not only `kind/reagent`
- support for JS & CSS deps from github repos
- page generation - minor refactoring
- updated Kindly version: `"4-alpha18"`
- `kind/htmlwidgets-ggplotly` support - WIP

## [2-alpha84] - 2024-03-16
- bugfix in `kind/table` preparation: dissocing irrelevant details correctly
- simpler handling of plain values inside `kind/table` - solving some of the crashes of big tables and making the resulting HTML more lightweight

## [2-alpha83] - 2024-03-15
- more input kinds supported by `kind/table` (PR by @genmeblog)
  - added `seq-of-seqs`, `seq-of-maps` and `map-of-seqs` with better infering column names for `seq-of-maps`

## [2-alpha82] - 2024-03-14
- changed default Quarto theme to `cosmo`
- support for dedicated CSS classes of certain items (e.g., "clay-image")
- images are styled through classes, keeping original size by default
- support for `kind/table` with no head
- support for nested kinds in `kind/table` column names
- bugfix: passing fallback preparer inside fragments (#85)
- bugfix: handling markdown generation consistently when a js script is included
- bugfix: using Quarto's data-qmd approach only inside tables (#80)
- file management - cleaning up memory to avoid memory leakage (#84)
- support for `.ipynb` Jupyter notebooks in books (#77)

## [2-alpha81] - 2024-02-24
- bugfix: typo in handling :kindly/options of nested elements
- bugfix: assigning the full target path to a book's index page
- bugfix: avoiding global quarto title in book chapters
- bugfix: making sure all book pages are wrapped with header and live-reload script
- returning to main page on reload

## [2-alpha80] - 2024-02-17
- avoiding the iframe when serving the page (simplifies URL handling, etc.)
- slight changes in styling
- handling space lines between comments carefully
- passing content-type information on the web server
- `kind/observable` ([Observable](https://observablehq.com/)) support (when rendering through Quarto)

## [2-alpha79] - 2024-02-08
- handling element max height in markdown
- refactoring item preparation, making sure datatables.js works in Quarto in the new setup
- bugfix: making sure a book index page is available if not provided
- added `.table-hover` & `.table-responsive` classes for `kind/table`

## [2-alpha78] - 2024-02-04
- using Quarto raw-html and data-qmd support (thus supporting formulae in tables) - fixing #54
- showing keywords correctly in column names - fixing #52
- some code simplification

## [2-alpha77] - 2024-02-02
- `kind/fragment` support
- avoiding `:element/max-size` by default
- supporting kinds that hide code globally
- removed the obsolete `scicloj.clay.v2.book` ns and the corresponding API function

## [2-alpha76] - 2024-01-28
- using a temporary Portal build to enjoy some fixes
- not relying on Portal for syntax highlighting anymore

## [2-alpha75] - 2024-01-27
- added a dedicated namespace of Quarto themes
- added a dedicated namespace of Quarto code highlight styles

## [2-alpha74] - 2024-01-27
- added leaflet-providers support

## [2-alpha73] - 2024-01-26
- added d3 js dependency

## [2-alpha72] - 2024-01-21
- removed highlighting theme from default config - using the Quarto default now
- added missing css (forgot to commit in `"2-alpha70"`)

## [2-alpha71] - 2024-01-21
- bugfix - adding a Portal item to ensure Portal syntax highlight on `[:html]` targets

## [2-alpha70] - 2024-01-21
- more styling of `[:html]` target - combining Bootswatch Spacelab with the `bootstrap.min.css` generated by Quarto

## [2-alpha69] - 2024-01-21
- updated highlight.js (version 11.9.0) - but we'll use it through the standalone Portal bundle for now
- code style change for `[:html]` target - using Portal's syntax highlighting
- using Bootswatch Spacelab theme for `[:html]` target - adapted for lighter bg-light

## [2-alpha68] - 2024-01-18
- `kind/video` support

## [2-alpha67] - 2024-01-17
- reagent deps are now expressed through kindly options

## [2-alpha66] - 2024-01-15
- using deep merge when merging configurations

## [2-alpha65] - 2024-01-14
- removed unnecessary printing

## [2-alpha64] - 2024-01-14
- nested kinds in `kind/map`:
  - bugfix: some kv pairs were skipped
  - styling change

## [2-alpha63] - 2024-01-14
- expressing classes in Quarto more idiomatically (using `::: ... :::` rather than explicit `div`s)
- the target directory can now be cleaned up by explicitly specifying `:clean-up-target-dir true`, but not by default on books as it used to be.

## [2-alpha62] - 2024-01-12
- bugfix: passing `:kindly/options` to the item context
- cleaned up the default table.css, for now 
- limiting the height of output elements by default
- the subdirectories to synchronize with targets are now configurable through `:subdirs-to-sync`

## [2-alpha61] - 2024-01-11
- updated vega-lite versions (#34)

## [2-alpha60] - 2024-01-11
- support for `:kind/smile-model`
- hiding the ouptout of calls to `require`

## [2-alpha59] - 2024-01-11
- updated some deps
- improved book making: taking care of rendering and repositioning the rendered files

## [2-alpha58] - 2024-01-08
- fixed the handling of `:kindly/options` in context
- changed the default behaviour of `kind/table` (WIP)

## [2-alpha57] - 2024-01-05
- adapting to Kindly changes: kindly options are passed as metadata

## [2-alpha56] - 2024-01-03
- bugfix: marking table class when nesting markdown inside other structures

## [2-alpha55] - 2024-01-01
- minor refactoring for clarity
- cancelled opinionated table styling
- bugfix: using real paths when figuring out the path relative to repo

## [2-alpha54] - 2023-12-24
- minor changes to Calva custom REPL commands: names, printing, etc.
- updating to Kindly version `4-alpha9` supports, among other things, an `option` argument to kinds, and an API extension for hiding code
- breaking changes in the way options are passed to `kind/cytoscape`, `kind/echarts`, `kind/plotly` -- now we use `[spec options]` rather than `[options spec]`
- datatables options support for `kind/table`

## [2-alpha53] - 2023-12-14
- Calva custom REPL commands

## [2-alpha52] - 2023-12-12
- kindly version update

## [2-alpha51] - 2023-12-10
- bugfix: passing full context in recursive item preparation

## [2-alpha50] - 2023-12-10
- When vega/vega-lite data is given in CSV format, Clay will serve it in a separate CSV file alongside the generated HTML.

## [2-alpha49] - 2023-12-04
- fixed broken welcome message on `start!`
- fixed the preparation of :kind/map - just print where possible
- made `:format [:html]` the default
- support for `make!` with a single value, not just a single form

## [2-alpha48] - 2023-12-04
- making sure the base-target-directory exists before synching resources

## [2-alpha47] - 2023-12-04
- missing babashka.fs dependency

## [2-alpha46] - 2023-12-04
- allowing to `make!` a single form without specifying a source file
- synching resources more carefully -- not copying clj/cljs/cljc

## [2-alpha45] - 2023-12-03
- bugfix in syncing resources: using the correct spec

## [2-alpha44] - 2023-12-02
- updated deps
- saving BufferedImage object as png rather than jpg (because a BufferedImage resulting from png might not be savable as jpg https://stackoverflow.com/a/2290430)
- catching failures in saving images

## [2-alpha43] - 2023-12-01
- missing depdendency

## [2-alpha42] - 2023-12-01
- only start the server if show is not false
- remove warning about replacing get
- report target path on make
- remove deref printing
- support for multiple sources in `make!`
- refactoring of parameter flow
- displaying errors on the browser view
- support for markdown files in books through `make!`

## [2-alpha41] - 2023-11-25
- using `qmd` rather than `md` for Quarto files (to support R code blocks, etc.)

## [2-alpha40] - 2023-11-25
- a revised API, with one main entry point, `make!`
- changed the way the server works: always serving a file
- simplified the pathway for writing HTML
- using an iframe for the browser view
- changed the target paths to always end with "index.md" or "index.html"
- embedding images as separate files

## [2-alpha39] - 2023-11-03
- major cleanup and refactoring
- temporarily not supporting reagent-based items
- updated kindly version
- updated portal support
  - portal is now supported as a kind
  - updated portal dep (no longer relying on an ad-hoc patched version)
- raw html support
- unifying the quarto pathways in the API: one markdown generation funciton, two different actions (with/without rendering)
- new config-oriented actions (WIP)
- using file-based config rathern than stateful options
- added header to browser view

## [2-alpha38] - 2023-10-03
- path changes in light quarto book generation
- lightweight tables support
- nesting kinds in hiccup (WIP)
- updated datatables version
- bugfix in the custom walk function (called `outer` by mistake)
- bugfix in nested table view - avoiding conversion to string before handling kinds
- passing printed datasets as markdown on qmd pages

## [2-alpha37] - 2023-09-22
- minor change in dataset viewer to improve layout

## [2-alpha36] - 2023-09-21
- fixed typo in light quarto setup
- light qmd generation - code and output cleanup
- info-line is only at the bottom now
- minor change in dataset viewer
- changes in target paths

## [2-alpha35] - 2023-09-17
- lightweight qmd generateion (WIP)
- updated deps
- bugfix: missing deps in qmd generation
- using Portal as a js library without scittle

## [2-alpha34] - 2023-09-14
- draft support for embedded portal

## [2-alpha33] - 2023-09-08
- simplifying the use of kindly advice
- adapting to kindly ver 4-alpha3
- changing default quarto options for performance

## [2-alpha32] - 2023-08-31
- adapting to kindly v4
- removing obsolete `checks` ns

## [2-alpha31] - 2023-08-09
- silently handling path-related Exceptions (which currently happen in Windows)

## [2-alpha30] - 2023-08-07
- bugfix: using full path in the search for git project root

## [2-alpha29] - 2023-08-07
- avoiding the use of local path as title (#21)
- refactoring the handling of kinds:
  - adapting to kindly-default version 1-alpha10, where plain data structures have explicit kinds
  - handling hierarchy in a more clean and systematic way
- bug fix: avoiding looking for KaTeX fonts locally (which would take more care to work correctly)

## [2-alpha28] 2023-06-19
- minor aesthetic changes

## [2-alpha27] - 2023-06-07
- leaflet support
- more flexible caching (WIP)
- minor changes to info-line
- rendering more parts as plain-html
- moved the `is->` function to the kindly-default library

## [2-alpha26] - 2023-06-04
- simpler and more robust path handling in info-line

## [2-alpha25] - 2023-06-04
- catching a possible Exception in the method to recognize the current file

## [2-alpha24] - 2023-06-04
- added souce info-line to the generated doc'

## [2-alpha23] - 2023-05-30
- clojute.test support

## [2-alpha22] - 2023-04-25
- 3dmol support

## [2-alpha21] - 2023-04-24
- passing :kind/code more directly to quarto for better rendering
- bugfix: bringing back the handling of :kind/hiccup, which was dropped by mistake

## [2-alpha20] - 2023-04-24
- minor API extension: handling a value
- allowing to hide code by value metadata
- handling :kind/void
- passing :kind/md more directly to quarto for better rendering
- avoiding recursion in API calls

## [2-alpha19] - 2023-04-21
- Katex support

## [2-alpha18] - 2023-04-21
- handling hidden code blocks differently in the quarto pathway, to avoid quarto slowness
- added Emmy, tmdjs support

## [2-alpha17] - 2023-04-16
- ignoring missing source maps till we figure this problem out
- plotly support, customizable echarts element height
- serving binary files correctly

## [2-alpha16] - 2023-04-06
- simplified the way forms are sent to Clay (avoiding `tap>`)
- escaping printed values for html

## [2-alpha15] - 2023-04-05
- experimental quarto support
- using the Scicloj Scittle fork for MathBox.cljs support
- a more graceful user experience around main actions
- updated API entry points for a few of the main actions

## [2-alpha14]  - 2023-02-21
- table view support for datasets

## [2-alpha13]  - 2023-02-20
- supporting :kind/vega-lite
- using markdown again when printing datasets (following fix https://github.com/nextjournal/markdown/issues/12)

## [2-alpha12]  - 2022-12-17
- more consistent choice of ports

## [2-alpha11]  - 2022-12-16
- extended the API with a few url-related functions
- updated some client-side deps

## [2-alpha10]  - 2022-12-08
- making sure the selected communication port is free

## [2-alpha9]  - 2022-11-10
- adapting to Kindly changes - returning multiple contexts
- bugfix: using fallback viewer

## [2-alpha8]  - 2022-11-06
- updated Kindly deps

## [2-alpha7]  - 2022-10-30
- temporarily avoiding some problems with dataset rendering

## [2-alpha6]  - 2022-10-29
- adapting to the extraction of kindly-default out of kindly

## [2-alpha5]  - 2022-10-27
- kindly version update

## [2-alpha4]  - 2022-10-23
- more careful kind handling in view
- namespace cleanup & refactoring
- added show-doc-and-write-html! to api
- bugfix in some reader edge cases
- make pprint the fallback case again
- kindly update

## [2-alpha3] - 2022-10-17
- adapting to Kindly changes
- minor visual changes

## [2-alpha2] - 2022-10-12
- dropped the cybermonday dependency

## [2-alpha1] - 2022-10-07
- rendeding documents without relying on Clerk
- switched to clay.v2 namespaces
- switched to kindly v3

## [1-alpha15] - 2022-08-07
- catching errors
- pretty printing where appropriate

## [1-alpha14] - 2022-05-09
- a slight change of the integration with CIDER/Calva (passing more info)
- making certain forms automatically hidden when showing a single value

## [1-alpha13] - 2022-05-06
- using `tap>` rather than nREPL middleware to listen to user evaluations
- fixed the support for code metadata in some cases

## [1-alpha12] - 2022-05-05
- patched clerk to pass code metadata freely
- support for code metadata in scittle doc preparation
- support for extensions setup on start; added dataset, clojisr setup as extensions

## [1-alpha11] - 2022-04-24
- scittle tool: reordered js lib rendering, added visual spacing

## [1-alpha10] - 2022-04-24
- scittle tool:
  - refactoring of page generation
  - optional loading of special widgets
  - more self-contained generated html (relying on less web resources)
  - made table-of-contents optional

## [1-alpha9] - 2022-04-23
- various aesthetics changes: bootswatch, tables, tech.ml.dadaset datasets, markdown, table-of-contents, etc.

## [1-alpha8] - 2022-04-22
- making sure kind/hidden is defined

## [1-alpha7] - 2022-04-22
- updated clerk version
- handling naive printing of values better

## [1-alpha6] - 2022-04-21
- updated scittle version
- more customizable document rendering
- more sensible table rendering

## [1-alpha5] - 2022-04-18
- fixed handling delays on scittle document rendering

## [1-alpha4] - 2022-04-17
- various changes in styling & minor API extensions
- Scittle document rendering

## [1-alpha3] - 2022-04-17
minor API extension

## [1-alpha2] - 2022-04-17
initial version of scittle viewer

## [1-alpha1] - 2022-04-08
initial version
