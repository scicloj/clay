(ns scicloj.clay.v2.quarto
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :as sh]
   [clojure.string :as string]
   [scicloj.clay.v2.page :as page]
   [scicloj.clay.v2.path :as path]
   [scicloj.clay.v2.server :as server]
   [scicloj.clay.v2.state :as state]
   [scicloj.clay.v2.show :as show]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.notebook :as notebook]
   [scicloj.clay.v2.util.time :as time]
   [clj-yaml.core :as yaml]))

(defn render-quarto! [items]
  (let [md-path (path/ns->target-path "docs/" *ns* ".md")
        html-path (-> md-path
                      (string/replace #"\.md$" ".html"))]
    (show/show-items! [item/loader])
    (io/make-parents md-path)
    (-> @state/*state
        (assoc :items items)
        page/md
        (->> (spit md-path)))
    (println [:wrote md-path (time/now)])
    (Thread/sleep 500)
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
        (assoc :items items)
        page/md
        (->> (spit md-path)))
    (update-quarto-config! chapter-path)
    (println [:wrote md-path (time/now)])))



(defn source-path->target-path [path]
  (-> path
      (string/replace #"\.clj$"
                      (if (-> path
                              path/path->filename
                              (= "index.clj"))
                        ".md"
                        "/index.md"))))

(defn source-path->file-ext [path]
  (-> path
      (string/split #"\.")
      last))

(defn ->base-book-config [{:keys [title chapter-source-paths]}]
  (let [index-included? (->> chapter-source-paths
                             (some #{"index.md" "index.clj"}))]
    {:project {:type "book"}
     :format {:html {:theme "cosmo"}}
     :book {:title title
            :chapters (->> (if index-included?
                             chapter-source-paths
                             (cons "index.md" chapter-source-paths))
                           (mapv source-path->target-path))}}))

(defn ->main-index [{:keys [toc embed-resources title]}]
  (str "---\n"
       (yaml/generate-string {:format {:html {:toc toc}}
                              :embed-resources embed-resources})
       "\n---\n"
       "# " title))

(defn update-book! [{:keys [base-source-path
                            chapter-source-paths
                            base-target-path
                            title
                            quarto-book-config
                            page-options
                            embed-resources
                            main-index]
                     :or {title "Book Draft"
                          page-options {:toc true}
                          embed-resources true
                          quarto-book-config (->base-book-config {:title title
                                                                  :chapter-source-paths chapter-source-paths})
                          main-index (->main-index {:toc (:toc page-options)
                                                    :embed-resources embed-resources
                                                    :title title})}}]
  (let [config-path (str base-target-path "/_quarto.yml")
        main-index-path (str base-target-path "/index.md")]
    (io/make-parents config-path)
    (->> quarto-book-config
         yaml/generate-string
         (spit config-path))
    (prn [:created config-path])
    (when-not (-> main-index-path io/file .exists)
      (spit main-index-path main-index)
      (prn [:created main-index-path])))
  (->> chapter-source-paths
       (map (fn [source-path]
              (let [target-path (-> source-path
                                    source-path->target-path)
                    full-source-path (str base-source-path "/" source-path)
                    full-target-path (str base-target-path "/" target-path)]
                (io/make-parents full-target-path)
                (case (source-path->file-ext source-path)
                  "md" (->> full-source-path
                            slurp
                            (spit full-target-path))
                  "clj" (-> @state/*state
                            (assoc :items (-> full-source-path
                                              (notebook/gen-doc {:title source-path})))
                            (update :options merge page-options)
                            page/md
                            (->> (spit full-target-path))))
                (prn [:wrote full-target-path (time/now)]))))))
