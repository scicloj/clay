[![Clojars Project](https://img.shields.io/clojars/v/org.scicloj/clay.svg)](https://clojars.org/org.scicloj/clay)
# Clay

![Clay logo](resources/Clay.svg)

Clay is a REPL-friendly Clojure tool for a dynamic workflow of data visualization and literate programming.

It also offers a set of tools to integrate Clojure literate programming with the [Quarto](https://quarto.org/) publishing system.

## Getting Started

Check out [Get Started](https://scicloj.github.io/clay/#getting-started) to get started!

## Docs

More details may be found in the [Documentation](https://scicloj.github.io/clay/). In particular:
* See the [API](https://scicloj.github.io/clay#api) and [Configuration](https://scicloj.github.io/clay#configuration) subsections for more options and variations.
* See the [Setup](https://scicloj.github.io/clay#setup) section and recent [Videos](https://scicloj.github.io/clay#videos) for details about integrating Clay with your editor so you do not need to call `make!` yourself.

## Related projects

The [Claykind](https://github.com/timothypratley/claykind) project is rethinking the Clay architecture and implementation from scratch, in a new code base. Currently (Fall 2023), the two projects are being developed in coordination.

[Kindly](https://scicloj.github.io/kindly-noted/kindly) is a common ground for defining how things should be visualized, seeking compatibility across tools.

[kindly-advice](https://scicloj.github.io/kindly-noted/kindly_advice) is a library that helps tools such as Clay to be Kindly-compatible.

[kindly-render](https://github.com/scicloj/kindly-render) is a library for rendering kinds to different taget formats. In the near future (as of Nov. 2024), Clay should rely on it.

[read-kinds](https://github.com/scicloj/read-kinds) is used internally by Claykind (and soon by Clay as well) to generate Kindly advice from notebooks expressed as Clojure namespaces.

## Projects using Clay

Please reach out to add your project to this list.

;; ## Projects using Clay

- [Tablecloth documentation](https://scicloj.github.io/tablecloth/)
- [Wolframite documentation](https://scicloj.github.io/wolframite)
- [Clay documentation](https://scicloj.github.io/clay/)
- [Kindly-noted](https://scicloj.github.io/kindly-noted/) - documenting the ecosystem around Kindly
- [Noj documentation](https://scicloj.github.io/noj/)
- [Tableplot documentation](https://scicloj.github.io/tableplot/)
- [Fastmath 3 documentation](https://generateme.github.io/fastmath/clay)
- [Tablemath documentation](https://scicloj.github.io/tablemath/)
- [ClojisR documentation](https://scicloj.github.io/clojisr/)
- [Clojure Tidy Tuesdays](https://kiramclean.github.io/clojure-tidy-tuesdays/) data-science explorations
- [Clojure Data Tutorials](https://scicloj.github.io/clojure-data-tutorials/)
- [Clojure Data Scrapbook](https://scicloj.github.io/clojure-data-scrapbook/)
- [LLMs tutorial](https://kpassapk.github.io/llama.clj/llama.html) (in spanish) by Kyle Passarelli
- [Statistical Computing in Clojure: Functional Approaches to Unsupervised Learning](https://github.com/adabwana/f24-cs7300-final-project/) by Jaryt Salvo
- the [SciNoj Light](https://scicloj.github.io/docs/community/groups/scinoj-light/) conference

## Discussion

Regular updates are given at the [visual-tools meetings](https://scicloj.github.io/docs/community/groups/visual-tools/).

The best places to discuss this project are:
* a topic thread under the [#clay-dev stream](https://clojurians.zulipchat.com/#narrow/stream/422115-clay-dev) at the Clojurians Zulip (more about chat streams [here](https://scicloj.github.io/docs/community/chat/)) 
* a [github issue](https://github.com/scicloj/clay/issues)
* a thread at the [visual-tools channel](https://clojurians.slack.com/archives/C02V9TL2G3V) of the Clojurians slack

![quaternary clay in Estonia](https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg)
(credit: [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:Clay-ss-2005.jpg))

## License

Copyright © 2025 Scicloj

_EPLv1.0 is just the default for projects generated by `clj-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.
