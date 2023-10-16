(ns scicloj.clay.v2.quarto
  (:require [scicloj.clay.v2.path :as path]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [scicloj.clay.v2.page :as page]
            [scicloj.clay.v2.state :as state]
            [scicloj.clay.v2.time :as time]
            [scicloj.clay.v2.server :as server]
            [clojure.java.shell :as sh]))

(defn render-quarto! [items]
  (let [md-path (path/ns->target-path "docs/" *ns* "_quarto.md")
        html-path (-> md-path
                      (string/replace #"\.md$" ".html"))]

    (io/make-parents md-path)
    (-> @state/*state
        (page/qmd items)
        (->> (spit md-path)))
    (println [:wrote md-path (time/now)])
    (->> (sh/sh "quarto" "render" md-path)
         ((juxt :err :out))
         (mapv println))
    (println [:created html-path (time/now)])
    (state/reset-quarto-html-path! html-path)
    (server/broadcast! "refresh")
    :ok))



(def base-quarto-config
  "
project:
  type: book

format:
  html:
    theme: cosmo

book:
  title: \"book\"
  chapters:
    - index.md
")

(def base-quarto-index
  "
---
format:
  html: {toc: true}
embed-resources: true
---
# book index
  ")

(defn update-quarto-config! [chapter-path]
  (let [index-path "book/index.md"
        config-path "book/_quarto.yml"
        current-config (if (-> config-path io/file .exists)
                         (slurp config-path)
                         (do (spit config-path base-quarto-config)
                             (println [:created config-path])
                             base-quarto-config))
        chapter-line (str "    - " chapter-path)]
    (when-not (-> index-path io/file .exists)
      (spit index-path base-quarto-index)
      (println [:created index-path]))
    (when-not (-> current-config
                  (string/split #"\n")
                  (->> (some (partial = chapter-line))))
      (->> chapter-line
           (str current-config "\n")
           (spit config-path))
      (println [:updated config-path
                :with chapter-path]))))

(defn write-quarto! [items]
  (let [chapter-path (if (-> *ns*
                             str
                             (string/split #"\.")
                             last
                             (= "index"))
                       (path/ns->target-path "" *ns* ".md")
                       (path/ns->target-path "" *ns* "/index.md"))
        md-path (str "book/" chapter-path)]
    (io/make-parents md-path)
    (-> @state/*state
        (page/qmd items)
        (->> (spit md-path)))
    (update-quarto-config! chapter-path)
    (println [:wrote md-path (time/now)])))
