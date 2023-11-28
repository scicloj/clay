(ns scicloj.clay.v2.book
  (:require
   [clj-yaml.core :as yaml]
   [clojure.java.io :as io]
   [clojure.java.shell :as sh]
   [clojure.string :as string]
   [scicloj.clay.v2.config :as config]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.notebook :as notebook]
   [scicloj.clay.v2.page :as page]
   [scicloj.clay.v2.server :as server]
   [scicloj.clay.v2.util.path :as path]
   [scicloj.clay.v2.util.time :as time]))


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

(defn ->main-index [{:keys [toc title]}]
  (str "---\n"
       (yaml/generate-string {:format {:html {:toc toc}}})
       "\n---\n"
       "# " title))

(defn write-book-config! [quarto-book-config
                          {:keys [base-target-path]}]
  (let [config-path (str base-target-path "/_quarto.yml")]
    (io/make-parents config-path)
    (->> quarto-book-config
         yaml/generate-string
         (spit config-path))
    (prn [:created config-path])))

(defn write-main-book-index-if-needed! [main-index
                                        {:keys [base-target-path]}]
  (let [main-index-path (str base-target-path "/index.md")]
    (when-not (-> main-index-path io/file .exists)
      (spit main-index-path main-index)
      (prn [:created main-index-path]))))

(defn write-book-chapter! [source-path
                           {:keys [base-source-path
                                   base-target-path
                                   page-config]}]
  (let [target-path (-> source-path
                        source-path->target-path)
        full-source-path (str base-source-path "/" source-path)
        full-target-path (str base-target-path "/" target-path)]
    (io/make-parents full-target-path)
    (case (source-path->file-ext source-path)
      "md" (->> full-source-path
                slurp
                (spit full-target-path))
      "clj" (-> {:items (-> full-source-path
                            (notebook/notebook-items
                             {:title source-path
                              :target-path full-target-path}))
                 :config page-config}
                page/md
                (->> (spit full-target-path))))
    (prn [:wrote full-target-path (time/now)])))

(defn write-book! [{:keys [base-source-path
                           chapter-source-paths
                           base-target-path
                           title
                           quarto-book-config
                           page-config
                           main-index]
                    :or {title "Book Draft"
                         page-config {:toc true}
                         quarto-book-config (->base-book-config {:title title
                                                                 :chapter-source-paths chapter-source-paths})
                         main-index (->main-index {:toc (:toc page-config)
                                                   :title title})}}]
  (-> quarto-book-config
      (write-book-config! {:base-target-path base-target-path}))
  (->> chapter-source-paths
       (map (fn [source-path]
              (-> source-path
                  (write-book-chapter!
                   {:base-source-path base-source-path
                    :base-target-path base-target-path
                    :page-config page-config}))))
       doall)
  (-> main-index
      (write-main-book-index-if-needed! {:base-target-path base-target-path})))


(comment
  (write-book!
   {:title "Clay"
    :base-source-path "notebooks"
    :base-target-path "book"
    :chapter-source-paths ["index.clj"
                           "slides.clj"]
    :page-config {:quarto {:format {:html {;; Quarto themes:
                                           ;; https://quarto.org/docs/output-formats/html-themes.html
                                           :theme :spacelab
                                           :monofont "Fira Code Medium"}}
                           ;; Quarto code highlighting:
                           ;; https://quarto.org/docs/output-formats/html-code.html#highlighting
                           :highlight-style :solarized}}}))
