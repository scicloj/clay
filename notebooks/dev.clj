(ns dev
  (:require [scicloj.clay.v2.api :as clay]))

(clay/make! {:format [:quarto :html]
             :base-source-path "notebooks"
             :source-path "index.clj"
             :base-target-path "docs"
             :clean-up-target-dir true})
