# Clay

![Clay logo](resources/Clay.svg)

Clay is a small Clojure tool for a dynamic workflow of data visualization and literate programming.

It also offers a set of tools to integrate Clojure literate programming with the [Quarto](https://quarto.org/) publishing system.

## Getting Started
Add Clay to your project dependencies:

[Clojars Project](https://clojars.org/org.scicloj/clay/versions/2-beta12) - recommended version: 2-beta12

(If you like to use [aliases](https://practical.li/blog-staging/posts/clojure-cli-tools-understanding-aliases/), you may add under it to the extra dependencies under an alias. See, for example, the [deps.edn](https://github.com/scicloj/noj/blob/main/deps.edn) file of [Noj](https://github.com/scicloj/noj). If you do not know what this means, just add it under the main `:deps` section of your `deps.edn` file.)

To render a given Clojure namespace, say `"notebooks/index.clj"`, you may run the following in the REPL:
```clj
(require '[scicloj.clay.v2.api :as clay])
(clay/make! {:source-path "notebooks/index.clj"})
```
This will render an HTML page and serve it in Clay's browser view.
Note that Clay does not need to be mentioned in the namespace we are rendering.

## Docs

More details may be found in the [Documentation](https://scicloj.github.io/clay/). In particular:
* See the [API](https://scicloj.github.io/clay#api) and [Configuration](https://scicloj.github.io/clay#configuration) subsections for more options and variations.
* See the [Setup](https://scicloj.github.io/clay#setup) section and recent [Videos](https://scicloj.github.io/clay#videos) for details about integrating Clay with your editor so you do not need to call `make!` yourself.

## Related projects

The [Claykind](https://github.com/timothypratley/claykind) project is rethinking the Clay architecture and implementation from scratch, in a new code base. Currently (Fall 2023), the two projects are being developed in coordination.

[Kindly](https://scicloj.github.io/kindly-noted/kindly) is a common ground for defining how things should be visualized, seeking compatibility across tools.

[kindly-advice](https://scicloj.github.io/kindly-noted/kindly_advice) is a library that helps tools such as Clay to be Kindly-compatible.

[read-kinds](https://github.com/scicloj/read-kinds) is used internally by Claykind (and soon by Clay as well) to generate Kindly advice from notebooks expressed as Clojure namespaces.

## Projects using Clay

Please reach out to add your project to this list.

- [Tablecloth documentation](https://scicloj.github.io/tablecloth/)
- [ClojisR documentation](https://scicloj.github.io/clojisr/)
- [Clay documentation](https://scicloj.github.io/clay/)
- [Kindly-noted](https://scicloj.github.io/kindly-noted/) - documenting the ecosystem around Kindly - WIP
- [Noj documentation](https://scicloj.github.io/noj/) - WIP
- [Clojure Tidy Tuesdays](https://kiramclean.github.io/clojure-tidy-tuesdays/) data-science explorations
- [Clojure Data Scrapbook](https://scicloj.github.io/clojure-data-scrapbook/)
- [LLMs tutorial](https://kpassapk.github.io/llama.clj/llama.html) (in spanish) by Kyle Passarelli

## Discussion

Regular updates are given at the [visual-tools meetings](https://scicloj.github.io/docs/community/groups/visual-tools/).

The best places to discuss this project are:
* a topic thread under the [#clay-dev stream](https://clojurians.zulipchat.com/#narrow/stream/422115-clay-dev) at the Clojurians Zulip (more about chat streams [here](https://scicloj.github.io/docs/community/chat/)) 
* a [github issue](https://github.com/scicloj/clay/issues)
* a thread at the [visual-tools channel](https://clojurians.slack.com/archives/C02V9TL2G3V) of the Clojurians slack

![quaternary clay in Estonia](https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg)
(credit: [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:Clay-ss-2005.jpg))

## License

Copyright © 2022 Scicloj

_EPLv1.0 is just the default for projects generated by `clj-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.
