(ns dev
  (:require [scicloj.clay.v2.api :as clay]))

(clay/make! {:format [:quarto :html]
             :base-source-path "notebooks"
             :source-path ["index.clj"
                           "clay_book/examples.clj"
                           "clay_book/test_generation.clj"
                           #_"clay_book/emmy_viewers.clj"]
             :base-target-path "docs"
             :book {:title "Clay Documentation"}
             :clean-up-target-dir true})
