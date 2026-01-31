# Contributing to Clay

[Issues](https://github.com/scicloj/clay/issues) and pull requests are welcome.

It is recommended to discuss ideas in a topic thread under the [#clay-dev stream](https://clojurians.zulipchat.com/#narrow/stream/422115-clay-dev).

If you are interested in working on clay,
we'd love to help you get familiar with the code.
Maybe we can set up some pair programming sessions to help.

## Building index.clj

See [notebooks/dev.clj](notebooks/dev.clj)

## Concepts

Clay is built around the idea of a "context" (sometimes also called a "note").
A context is a map that contains the original code, the form, and the evaluated value.
The idea is that an input namespace is divided into top level "contexts".

**note** - the info about a form from our namespace
including the evaluation result
and the kindly advice

the code:

```clojure
(kind/hiccup
  (into [:div] (for [i (range 9)] [:p i])))
```

the note:
```clojure
{:code "...."
 :form '(kind/hiccup (into [:div] (for [i (range 9)] [:p i])))
 :value [:div [:p 0] [:p 1] ... [:p 8]]
 :kind :kind/hiccup}
```

`kindly-advice/advise` is responsible for generating the `:kind` part
by a process called "kind inference".

**item** - the info we need to render the note

form: `(kind/hiccup [:h1 "hello"])`

items: `[{:hiccup [:h1 "hello"] :dep []}]`

form: `(kind/md "# hello")`

items: `[{:md "# hello" :dep [:katex]}]`

form: `(kind/fragment [(kind/md "# hello") (kind/hiccup [:h1 "hello"])])`

items: `[{:hiccup [:h1 "hello"] :dep []} {:md "# hello" :dep [:katex]}]`

We can always convert md->hiccup, hiccup->md if we have one of them and need the other one.

When we generate a Quarto document (.qmd), we prefer markdown.

Whe we directly generate HTML, we prefer hiccup.

If we start with this notebook:

```clojure
(ns my.notebook)
;; this is a comment
(+ 1 2)
^kind/hiccup [:div "hello"]
```

We produce these contexts:

```clojure
[{:code "(ns my.notebook)" :form (ns my.notebook) :value nil}
 {:code ";; this is a comment"}
 {:code "(+ 1 2)" :form (+ 1 2) :value 3}
 {:code "[:div \"hello\"]" :form [:div "hello"] :value [:div "hello"]}]
```

Then these flow through a pipeline until finally text is emitted.

Kinds are identified (via kindly-advice):

```clojure
{:code "[:div \"hello\"]" :form [:div "hello"] :value [:div "hello"]}
=> {:kind :kind/hiccup :code "[:div \"hello\"]" :form [:div "hello"] :value [:div "hello"]}
```

Rendered items are produced:

```clojure
{:kind :kind/hiccup :code "[:div \"hello\"]" :form [:div "hello"] :value [:div "hello"]}
=> {:hiccup [:div "hello"]
    :deps #{}}

{:code ";; this is a comment"}
=> {:md "this is a comment"
    :hiccup [:p "this is a comment"]
    :deps #{}}
```

With this information, either a html file or a markdown file can be written.
When markdown is missing, the hiccup must be used (recall markdown can contain html).

`:deps` collects javascript dependencies for rendering as keywords.
These are mapped to urls or node packages so they can be included in the final output.

## Vision

Our aim is for clay to be a small part of a larger collection of libraries and tools that work together.

| project       | purpose                                             | needs                                |
|---------------|-----------------------------------------------------|--------------------------------------|
| kindly        | annotate visualizations                             | documentation                        |
| read-kinds    | read code, evaluate, produce contexts               | integration with clay                |
| kindly-advice | given a context, chooses a kind                     | refactoring                          |
| kindly-render | create items containing `:md` `:hiccup` and `:deps` | completion and integration with clay |
| clay          | manage files                                        | stability, promotion                 |
| quarto        | render markdown to html                             | mature                               |

Clay is the most visible and important project, tying everything together for users.

While we are working toward this vision, it is important to note that `scicloj.clay.v2.prepare` currently performs the role of `kindly-render`, and `scicloj.clay.v2.item` defines visualizations that cannot be nested.


## Areas for improvement

Clay can benefit from improved documentation, issue fixes, and refactoring.

* Documentation
  - what are all the valid configuration keys?
  - better page load experience
  - more inviting first impression
  - docstrings
* Issues
  - there are many open issues to investigate
* Refactoring to use `read-kinds` and `kindly-render`
  - bringing all functionality into those libraries
  - ensuring that clay continues to work correctly

Of course, if you have other ideas, let's discuss them.

## Wishlist

### Clay

* bugs
* improvements to the setup regarding files
* more clever book setup out of the box
* documentation:
  - a friendly intro into Clay and its use cases
  - the config
  - splitting the gallery into pages
  - a JS->clojure-map translation guide
  - internals (notes, items, etc.)
* support other clojure-like languages
 - babashka (read-kinds works with babashka)
 - jank
* test-generation
* bidirectional conversion from/to clj-in-markdown
* better UX with the browser view (what happens on an update)
* sharing common configuration workflows 

### Kindly and kindly-render

* support for more kinds: especially emmy.viewers
* dashboards module
* basic support for browser-backend interaction
* layout
* other tools: Portal, badspreadsheet, Jupyter
