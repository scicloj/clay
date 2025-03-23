(ns scicloj.clay.v2.make
  (:require [scicloj.clay.v2.config :as config]
            [scicloj.clay.v2.live-reload :as live-reload]
            [scicloj.clay.v2.util.path :as path]
            [scicloj.clay.v2.read :as read]
            [scicloj.clay.v2.item :as item]
            [scicloj.clay.v2.prepare :as prepare]
            [scicloj.clay.v2.notebook :as notebook]
            [scicloj.clay.v2.page :as page]
            [scicloj.clay.v2.server :as server]
            [scicloj.clay.v2.util.time :as time]
            [clojure.string :as string]
            [clojure.java.shell :as shell]
            [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [babashka.fs]
            [scicloj.clay.v2.util.fs :as util.fs]
            [clojure.string :as str]
            [scicloj.clay.v2.util.merge :as merge]
            [scicloj.clay.v2.files :as files]
            [clojure.pprint :as pp]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly-render.notes.to-html-page :as to-html-page]))

(defn spec->source-type [{:keys [source-path]}]
  (some-> source-path
          path/path->ext))

(defn spec->ns-form [{:keys [source-type full-source-path]}]
  (when (= source-type "clj")
    (-> full-source-path
        slurp
        read/read-ns-form)))


(defn spec->full-source-path [{:as spec
                               :keys [base-source-path source-path]}]
  (when source-path
    (cond
      ;; no source path
      (nil? source-path)
      nil
      ;; simply a path
      (string? source-path)
      (or (some-> base-source-path
                  (str "/" source-path))
          source-path)
      ;; else
      :else
      (throw (ex-info (str "Invalid source path: " (pr-str source-path))
                      {:source-path source-path})))))


(defn spec->full-target-path [{:as spec
                               :keys [full-source-path
                                      source-type
                                      base-target-path
                                      format
                                      ns-form
                                      single-form
                                      single-value]}]
  (cond
    ;; temporary target
    (or single-value
        single-form
        (nil? source-type))
    (str base-target-path
         "/.clay.html")
    ;; simply a path
    (string? full-source-path)
    (case source-type
      "md" (str base-target-path
                "/"
                full-source-path)
      "Rmd" (str base-target-path
                 "/"
                 full-source-path)
      "ipynb" (str base-target-path
                   "/"
                   full-source-path)
      "clj" (path/ns->target-path base-target-path
                                  (or (some-> ns-form
                                              second
                                              name)
                                      "unnamed")
                                  (str (when (-> format
                                                 second
                                                 (= :revealjs))
                                         "-revealjs")
                                       ".html")))
    ;; else
    :else
    (throw (ex-info "invalid full source path"
                    {:full-source-path full-source-path}))))

(defn spec->ns-config [{:keys [ns-form]}]
  (some-> ns-form
          meta
          :clay))

(defn merge-ns-config [spec]
  (merge/deep-merge
   spec
   (spec->ns-config spec)))

(defn ->single-ns-spec [spec
                        config-and-spec
                        source-path]
  (-> config-and-spec
      (assoc :source-path source-path)
      (config/add-field :full-source-path spec->full-source-path)
      (config/add-field :source-type spec->source-type)
      (config/add-field :ns-form spec->ns-form)
      merge-ns-config
      (merge/deep-merge
       (dissoc spec :source-path)) ; prioritize spec over the ns config
      (config/add-field :full-target-path spec->full-target-path)))

(defn extract-specs [config spec]
  (let [{:as config-and-spec :keys [source-path]}
        (merge/deep-merge config spec) ; prioritize spec over global config
        ;;
        source-paths (if (sequential? source-path)
                       source-path
                       [source-path])
        ;; collect specs for single namespaces,
        ;; keeping the book parts structure, if any
        single-ns-specs-w-book-struct (->> source-paths
                                           (map (fn [path]
                                                  (cond
                                                    ;; just a path or no path
                                                    (or (string? path)
                                                        (nil? path))
                                                    (->single-ns-spec spec
                                                                      config-and-spec
                                                                      path)
                                                    ;; a book part
                                                    (:part path)
                                                    (-> path
                                                        (update :chapters
                                                                (partial
                                                                 map
                                                                 (fn [chapter-path]
                                                                   (->single-ns-spec spec
                                                                                     config-and-spec
                                                                                     chapter-path)))))
                                                    ;; else
                                                    :else
                                                    (throw (ex-info (str "Invalid source path: " (pr-str path))
                                                                    {:path path}))))))]
    {:main-spec (-> config-and-spec
                    (assoc :full-target-paths-w-book-struct
                           (->> single-ns-specs-w-book-struct
                                (map (fn [ns-spec]
                                       (if (:part ns-spec)
                                         (-> ns-spec
                                             (update :chapters
                                                     (partial map :full-target-path)))
                                         (:full-target-path ns-spec))))))
                    (config/add-field :full-target-paths
                                      (fn [{:keys [full-target-paths-w-book-struct]}]
                                        (->> full-target-paths-w-book-struct
                                             (mapcat (fn [path]
                                                       (if (:part path)
                                                         (:chapters path)
                                                         [path])))))))
     :single-ns-specs (->> single-ns-specs-w-book-struct
                           ;; flatten book chapters:
                           (mapcat (fn [ns-spec]
                                     (if (:part ns-spec)
                                       (:chapters ns-spec)
                                       [ns-spec]))))}))


(defn index-path? [path]
  (some-> path
          (string/split #"/")
          last
          (#{"index.qmd" "index.clj"})))

(defn index-target-path? [path]
  (some-> path
          (string/split #"/")
          last
          (#{"index.html"})))

(defn spec->quarto-book-chapters-config [{:keys [base-target-path
                                                 full-target-paths
                                                 full-target-paths-w-book-struct
                                                 book]}]
  (let [index-included? (->> full-target-paths
                             (some index-target-path?))
        ->chapter-qmd-path (fn [full-target-path]
                             (-> full-target-path
                                 (string/replace (re-pattern (str "^"
                                                                  base-target-path
                                                                  "/"))
                                                 "")
                                 (string/replace #"\.html$"
                                                 ".qmd")))]
    (-> (->> full-target-paths-w-book-struct
             (map (fn [path]
                    (if (:part path)
                      (-> path
                          (update :chapters
                                  (partial map ->chapter-qmd-path)))
                      (->chapter-qmd-path path)))))
        (cond->> (not index-included?)
          (cons "index.qmd")))))

(defn spec->quarto-book-config [{:as spec
                                 :keys [book
                                        quarto]}]
  (-> quarto
      (select-keys [:format])
      (merge/deep-merge
       {:project {:type "book"}
        :book (merge {:chapters (spec->quarto-book-chapters-config spec)}
                     book)})))

(defn write-quarto-book-config! [quarto-book-config
                                 {:keys [base-target-path]}]
  (let [config-path (str base-target-path "/_quarto.yml")]
    (io/make-parents config-path)
    (->> quarto-book-config
         yaml/generate-string
         (spit config-path))
    (prn [:wrote config-path])
    [:wrote config-path]))

(defn quarto-book-index [{{:keys [toc title]} :book}]
  (str "---\n"
       (yaml/generate-string {:format {:html {:toc (some? toc)}}})
       "\n---\n"
       "# " title))

(defn write-quarto-book-index-if-needed! [quarto-index
                                          {:keys [base-target-path]}]
  (let [main-index-path (str base-target-path "/index.qmd")]
    (if-not (-> main-index-path io/file .exists)
      (do (spit main-index-path quarto-index)
          (prn [:wrote main-index-path])
          [:wrote main-index-path])
      [:ok])))

(defn make-book! [{:as spec
                   :keys [base-target-path
                          run-quarto
                          show]}]
  [(-> spec
       spec->quarto-book-config
       (write-quarto-book-config! spec))
   (-> spec
       quarto-book-index
       (write-quarto-book-index-if-needed! spec))
   (when run-quarto
     (prn [:render-book])
     (->> (shell/sh "quarto" "render")
          (shell/with-sh-dir base-target-path)
          ((juxt :err :out))
          (mapv (partial println "Clay Quarto:")))
     (babashka.fs/copy-tree (str base-target-path "/_book")
                            base-target-path
                            {:replace-existing true})
     (babashka.fs/delete-tree (str base-target-path "/_book"))
     (when show
       (-> spec
           (assoc :full-target-path (str base-target-path "/index.html"))
           server/update-page!))
     [:ok])])


(defn write-test-forms-as-ns [forms]
  (let [path (-> forms
                 (->> (filter notebook/ns-form?))
                 first
                 second
                 str
                 (str/replace #"\." "/")
                 (str/replace #"-" "_")
                 (->> (format "test/%s.clj")))]
    (io/make-parents path)
    (->> forms
         (map (fn [form]
                (-> form
                    pp/pprint
                    with-out-str)))
         (str/join "\n\n")
         (spit path))
    [:wrote path]))


(defn handle-single-source-spec! [{:as spec
                                   :keys [source-type
                                          single-form
                                          single-value
                                          format
                                          full-source-path
                                          full-target-path
                                          show
                                          run-quarto
                                          book
                                          post-process
                                          use-kindly-render]}]
  (when (or (= source-type "clj")
            single-form
            single-value)
    (try
      (files/init-target! full-target-path)
      (if use-kindly-render
        (let [notebook {:notes (-> full-source-path
                                   slurp
                                   read/->notes)}]
          (-> spec
              (assoc :page (to-html-page/render-notebook notebook))
              server/update-page!)
          [:wrote-with-kindly-render full-target-path])
        (let [{:keys [items test-forms]} (notebook/items-and-test-forms
                                          spec)
              spec-with-items      (-> spec
                                       (assoc :items items))]
          [(case (first format)
             :hiccup (let [qmd-path (-> full-target-path
                                        (string/replace #"\.html$" ".edn"))]
                       (page/hiccup spec-with-items))
             :html (do (-> spec-with-items
                           (config/add-field :page (if post-process
                                                     (comp post-process page/html)
                                                     page/html))
                           server/update-page!)
                       [:wrote full-target-path])
             :quarto (let [qmd-path (-> full-target-path
                                        (string/replace #"\.html$" ".qmd"))
                           output-file (-> full-target-path
                                           (string/split #"/")
                                           last)]
                       (-> spec-with-items
                           (update-in [:quarto :format]
                                      select-keys [(second format)])
                           (update-in [:quarto :format (second format)]
                                      assoc :output-file output-file)
                           (cond-> book
                             (update :quarto dissoc :title))
                           page/md
                           (->> (spit qmd-path)))
                       (println "Clay:" [:wrote qmd-path (time/now)])
                       (when-not book
                         (if run-quarto
                           (do (->> (shell/sh "quarto" "render" qmd-path)
                                    ((juxt :err :out))
                                    (mapv (partial println "Clay Quarto:")))
                               (println "Clay:" [:created full-target-path (time/now)])
                               (when post-process
                                 (->> full-target-path
                                      slurp
                                      post-process
                                      (spit full-target-path)))
                               (-> spec
                                   (assoc :full-target-path full-target-path)
                                   server/update-page!))
                           ;; else, just show the qmd file
                           (-> spec
                               (assoc :full-target-path qmd-path)
                               server/update-page!)))
                       (vec
                        (concat [:wrote qmd-path]
                                (when run-quarto
                                  [full-target-path])))))
           (when test-forms
             (write-test-forms-as-ns test-forms))]))
      (catch Throwable e
        (-> spec
            (assoc :page (-> spec
                             (assoc :items [(item/pprint e)])
                             page/html))
            server/update-page!)
        (throw e))
      (finally (files/init-target! full-target-path)))))


(defn sync-resources! [{:keys [base-target-path
                               subdirs-to-sync]}]
  (doseq [subdir subdirs-to-sync]
    (when (babashka.fs/exists? subdir)
      (let [target (str base-target-path "/" subdir)]
        (when (babashka.fs/exists? target)
          (babashka.fs/delete-tree target))
        (io/make-parents target)
        (util.fs/copy-tree-no-clj subdir target)))))

(defn make! [spec]
  (let [config (config/config)
        {:keys [single-form single-value]} spec
        {:keys [main-spec single-ns-specs]} (extract-specs config spec)
        {:keys [ide browse show book base-target-path watch-dirs clean-up-target-dir live-reload]} main-spec
        source-paths (set (map :source-path single-ns-specs))]
    (when (and clean-up-target-dir
               (not (or single-form single-value)))
      (babashka.fs/delete-tree base-target-path))
    (sync-resources! main-spec)
    (when show
      (server/loading!))
    (let [info [(mapv handle-single-source-spec! single-ns-specs)
                  (when book
                    (make-book! main-spec))
                  (when live-reload
                    (live-reload/start! make! spec source-paths watch-dirs))]
          summary {:url (server/url)
                   :key "clay"
                   :title "Clay"
                   ;; TODO: Maybe we can remove 'reveal' when fixed in Calva
                   :reveal false
                   :info info}]
      (if (and ide (not= browse :browser))
        (tagged-literal 'cursive/html summary)
        summary))))


(comment
  (make! {:source-path ["notebooks/scratch.clj"]
          :use-kindly-render true}))
