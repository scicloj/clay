(ns scicloj.clay.v2.make
  (:require [babashka.fs :as fs]
            [scicloj.clay.v2.config :as config]
            [scicloj.clay.v2.live-reload :as live-reload]
            [scicloj.clay.v2.read :as read]
            [scicloj.clay.v2.item :as item]
            [scicloj.clay.v2.notebook :as notebook]
            [scicloj.clay.v2.page :as page]
            [scicloj.clay.v2.server :as server]
            [scicloj.clay.v2.util.time :as time]
            [clojure.java.shell :as shell]
            [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [scicloj.clay.v2.util.fs :as util.fs]
            [clojure.string :as str]
            [scicloj.clay.v2.files :as files]
            [clojure.pprint :as pp]
            [scicloj.kindly-render.notes.to-html-page :as to-html-page]
            [hashp.preload]
            [scicloj.kindly.v4.api :as kindly]))

(defn spec->source-type [{:keys [source-path]}]
  (some-> source-path (fs/extension)))

(defn spec->ns-form [{:keys [source-type full-source-path]}]
  (when (= source-type "clj")
    (-> full-source-path
        slurp
        read/read-ns-form)))


(defn spec->full-source-path
  "Returns the source-path relative to the current working directory (project root).
  This may be the base-source-path + the source-path when both are relative,
  or if source-path is absolute will be relativized without regard to a base-source-path.
  full-source-path needs to be relative to calculate git links."
  [{:as   spec
    :keys [base-source-path source-path]}]
  (when source-path
    (when-not (string? source-path)
      (throw (ex-info (str "Invalid source path: " (pr-str source-path))
                      {:id          ::invalid-source-path
                       :source-path source-path})))
    (if (fs/absolute? source-path)
      (str (fs/relativize (fs/absolutize ".") source-path))
      (if base-source-path
        (str (fs/path base-source-path source-path))
        source-path))))

(defn relative-source-path
  "Returns the source-path relative to the base-source-path,
  which is used to calculate the target path."
  [{:as   spec
    :keys [source-path
           base-source-path
           ns-form]}]
  (cond
    ;; Absolute paths within base-source-path
    (and (fs/absolute? source-path)
         (some-> base-source-path (util.fs/child? source-path)))
    (fs/relativize (fs/absolutize base-source-path) source-path)
    ;; Absolute path outside base-source-path
    (fs/absolute? source-path)
    (or (some-> ns-form second name (str/replace "." "/") (str/replace "-" "_") (str ".clj"))
        (fs/file-name source-path))
    :else
    source-path))

(defn tempory-target? [{:keys [source-type single-form single-value]}]
  (or single-value single-form))

(defn spec->full-target-path
  "Returns the target-path relative to the current working directory (project root)."
  [{:as   spec
    :keys [source-path
           full-source-path
           source-type
           base-target-path
           flatten-targets
           format
           keep-sync-root]}]
  (cond
    (tempory-target? spec)
    (str base-target-path "/.clay.html")

    (string? source-path)
    (let [relative-source (relative-source-path spec)]
      (str
       (case source-type
         ("md" "Rmd" "ipynb") (fs/path base-target-path
                                       (if keep-sync-root
                                         full-source-path
                                         relative-source))
         "clj" (let [target-extension (str (when (= (second format) :revealjs)
                                             "-revealjs")
                                           ".html")
                     relative-target (cond-> (str/replace relative-source
                                                          #"\.clj[cs]?$"
                                                          target-extension)
                                       flatten-targets (str/replace #"[\\/]+" "."))]
                 (fs/path base-target-path relative-target)))))))

(defn spec->qmd-target-path [{:as spec
                              :keys [format
                                     base-target-path
                                     full-target-path
                                     quarto-target-path]}]
  (when (= (first format) :quarto)
    (let [qmd-target (str/replace full-target-path #"\.html$" ".qmd")]
      (if (and quarto-target-path
               (not (tempory-target? spec)))
        (let [relative-target (fs/relativize base-target-path qmd-target)]
          (str (fs/path quarto-target-path relative-target)))
        qmd-target))))

(defn spec->ns-config [{:keys [ns-form]}]
  (some-> ns-form
          meta
          :clay))

(defn merge-ns-config [spec]
  (kindly/deep-merge
    spec
    (spec->ns-config spec)))

(defn maybe-user-hook [{:as spec :keys [config/transform]}]
  (if transform
    ((requiring-resolve transform) spec)
    spec))

(defn ->single-ns-spec [spec
                        config
                        source-path]
  (-> config
      (assoc :source-path source-path)
      (config/add-field :full-source-path spec->full-source-path)
      (config/add-field :source-type spec->source-type)
      (config/add-field :ns-form spec->ns-form)
      merge-ns-config
      (kindly/deep-merge
        (dissoc spec :source-path))                         ; prioritize spec over the ns config
      (config/add-field :full-target-path spec->full-target-path)
      (config/add-field :qmd-target-path spec->qmd-target-path)
      (maybe-user-hook)))

(defn extract-specs [config spec]
  (let [{:keys [source-paths]} config
        ;; collect specs for single namespaces,
        ;; keeping the book parts structure, if any
        single-ns-specs-w-book-struct (->> source-paths
                                           (map (fn [path]
                                                  (cond
                                                    ;; just a path or no path
                                                    (or (string? path)
                                                        (nil? path))
                                                    (->single-ns-spec spec config path)
                                                    ;; a book part
                                                    (:part path)
                                                    (-> path
                                                        (update :chapters
                                                                (partial
                                                                  map
                                                                  (fn [chapter-path]
                                                                    (->single-ns-spec spec config chapter-path)))))
                                                    ;; else
                                                    :else
                                                    (throw (ex-info (str "Invalid source path: " (pr-str path))
                                                                    {:path path}))))))]
    {:main-spec       (-> config
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


(defn index-target-path? [path]
  (some-> path
          (str/split #"/")
          last
          (#{"index.html"})))

(defn spec->quarto-book-chapters-config [{:keys [base-target-path
                                                 full-target-paths
                                                 full-target-paths-w-book-struct]}]
  (let [index-included? (->> full-target-paths
                             (some index-target-path?))
        ->chapter-qmd-path (fn [full-target-path]
                             (-> full-target-path
                                 (str/replace (re-pattern (str "^" base-target-path "/")) "")
                                 (str/replace #"\.html$" ".qmd")))]
    (-> (->> full-target-paths-w-book-struct
             (map (fn [path]
                    (if (:part path)
                      (-> path
                          (update :chapters
                                  (partial map ->chapter-qmd-path)))
                      (->chapter-qmd-path path)))))
        (cond->> (not index-included?)
          (cons "index.qmd")))))

(defn spec->quarto-book-config [{:as   spec
                                 :keys [book
                                        quarto]}]
  (-> quarto
      (select-keys [:format])
      (kindly/deep-merge
        {:project {:type "book"}
         :book    (merge {:chapters (spec->quarto-book-chapters-config spec)}
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

(defn quarto-render! [{:as spec
                       :keys [base-target-path
                              quarto-target-path
                              qmd-target-path
                              post-process
                              full-target-path
                              book]}]
  (let [temp (tempory-target? spec)
        quarto-root (if temp
                      base-target-path
                      (or quarto-target-path base-target-path))
        output-dir (and (not temp)
                        quarto-root
                        (fs/exists? (fs/path quarto-root "_quarto.yml"))
                        "_clay")
        input (delay
                (str (if quarto-root
                       (fs/relativize quarto-root qmd-target-path)
                       qmd-target-path)))
        cmd (cond-> ["quarto" "render"]
              (not book) (conj @input)
              output-dir (into ["--output-dir" output-dir]))
        _ (println (str "Clay sh [" quarto-root "]:") cmd)
        {:keys [out err exit]} (shell/with-sh-dir quarto-root
                                 (apply shell/sh cmd))]
    (when-not (str/blank? out)
      (println "Clay Quarto:\n" out))
    (when-not (str/blank? err)
      (println "Clay Quarto:\n" err))
    (when-not (zero? exit)
      (throw (ex-info "Clay Quarto failed."
                      {:id ::quarto-render-failed
                       :input input})))
    (when output-dir
      (let [output-path (fs/path quarto-root output-dir)]
        (when (fs/exists? output-path)
          (fs/copy-tree output-path base-target-path {:replace-existing true})
          (fs/delete-tree output-path)
          (println "Clay:" [:moved (str output-path) base-target-path (time/now)]))))
    (when post-process
      (->> full-target-path
           slurp
           post-process
           (spit full-target-path)))
    [:quarto-rendered full-target-path]))

(defn make-book! [{:as   spec
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
     (let [render-result (quarto-render! spec)]
       (prn [:render-result render-result]))
     (when show
       (-> spec
           (assoc :full-target-path (str base-target-path "/index.html"))
           server/update-page!))
     [:made-book])])


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

(defn maybe-run-quarto! [{:as spec
                          :keys [book
                                 run-quarto
                                 qmd-target-path
                                 format]}]
  (when (and (not book)
             (-> format first (= :quarto)))
    (if run-quarto
      (let [render-result (quarto-render! spec)]
        (println "Clay:" [:quarto-rendered render-result (time/now)])
        (server/update-page! spec)
        render-result)
      ;; else, just show the qmd file
      (server/update-page! (assoc spec :full-target-path qmd-target-path)))))


(defn clay-render-notebook [notes {:as spec
                                   :keys [format
                                          full-target-path
                                          qmd-target-path
                                          run-quarto
                                          book
                                          post-process]}]
  (let [{:keys [items test-forms exception]} (notebook/items-and-test-forms notes spec)
        spec-with-items (assoc spec
                               :items items
                               :exception exception)]
    [(case (first format)
       :hiccup (page/hiccup spec-with-items)
       :html (do (-> spec-with-items
                     (config/add-field :page (if post-process
                                               (comp post-process page/html)
                                               page/html))
                     server/update-page!)
                 (println "Clay: " [:wrote full-target-path (time/now)])
                 [:wrote full-target-path])
       :gfm (let [gfm-target (str/replace full-target-path #"\.html$" ".md")]
              (->> spec-with-items
                   page/gfm
                   (spit gfm-target))
              (println "Clay:" [:wrote gfm-target (time/now)])
              (server/update-page! (assoc spec :full-target-path gfm-target))
              [:wrote gfm-target])
       :quarto (do (-> spec-with-items
                       (update-in [:quarto :format] select-keys [(second format)])
                       (cond-> book (update :quarto dissoc :title))
                       page/md
                       (->> (spit qmd-target-path)))
                   (println "Clay:" [:wrote qmd-target-path (time/now)])
                   [:wrote qmd-target-path]))
     (when test-forms
       (write-test-forms-as-ns test-forms))
     (when exception
       (throw (ex-info "Notebook FAILED."
                       {:id ::notebook-exception}
                       exception)))]))

(defn kindly-render-notebook [notes {:as spec :keys [full-target-path]}]
  (let [notebook {:notes          notes
                  :kindly/options (kindly/deep-merge
                                    {:deps #{:kindly :clay :highlightjs}
                                     ;;:package ""
                                     }
                                    (:kindly/options spec))}]
    (-> spec
        (assoc :page (to-html-page/render-notebook notebook))
        server/update-page!)
    [:wrote-with-kindly-render full-target-path]))

(defn handle-single-source-spec! [{:as   spec
                                   :keys [source-paths
                                          source-type
                                          single-form
                                          single-value
                                          full-source-path
                                          full-target-path
                                          qmd-target-path
                                          use-kindly-render
                                          keep-existing
                                          external-requirements]}]
  (when (or (= source-type "clj")
            single-form
            single-value)
    (try
      (files/init-target! full-target-path)
      (let [skip (and external-requirements
                      keep-existing
                      qmd-target-path
                      (fs/exists? qmd-target-path))
            result (if skip
                     (do (println "Clay:" [:kept qmd-target-path])
                         [:kept qmd-target-path])
                     ;; else execute the notebook and render it
                     (let [notes (notebook/spec-notes spec)]
                       (if use-kindly-render
                         (kindly-render-notebook notes spec)
                         (clay-render-notebook notes spec))))]
        [result
         (maybe-run-quarto! spec)])
      (catch Throwable e
        (when-not (-> e ex-data :id (= ::notebook-exception))
          (-> spec
              (assoc :page (-> spec
                               (assoc :items [(item/print-throwable e false)])
                               page/html))
              server/update-page!))
        (if (and source-paths (> (count source-paths) 1))
          (do (println "Clay FAILED:" full-source-path)
              (println e))
          (throw e)))
      (finally (files/init-target! full-target-path)))))

(defn sync-resources! [{:keys [base-target-path
                               quarto-target-path
                               subdirs-to-sync
                               keep-sync-root]}]
  (doseq [subdir subdirs-to-sync
          target-path (if (and base-target-path quarto-target-path
                               (not= base-target-path quarto-target-path))
                        [base-target-path quarto-target-path]
                        [base-target-path])]
    (when (fs/exists? subdir)
      (let [target (if keep-sync-root
                     (fs/path target-path subdir)
                     target-path)]
        (if (= (fs/canonicalize target)
               (fs/canonicalize subdir))
          (println (format "Clay sync: not syncing \"%s\" to itself." subdir))
          (do (when (and keep-sync-root (fs/exists? target))
                (fs/delete-tree target))
              (util.fs/copy-tree-no-clj subdir target)))))))

(defn make! [spec]
  (let [config (config/config spec)
        {:keys [single-form single-value]} spec
        {:keys [main-spec single-ns-specs]} (extract-specs config spec)
        {:keys [ide browse show book base-target-path clean-up-target-dir live-reload]} main-spec
        source-paths (set (map :source-path single-ns-specs))]
    (when (and clean-up-target-dir
               (not (or single-form single-value)))
      (fs/delete-tree base-target-path))
    (sync-resources! main-spec)
    (when show
      (server/loading!))
    (let [info (cond-> (mapv handle-single-source-spec! single-ns-specs)
                 book (conj (make-book! main-spec))
                 live-reload (conj (if (#{:toggle} live-reload)
                                     (live-reload/toggle! make! main-spec source-paths)
                                     (live-reload/start! make! main-spec source-paths))))
          summary {:url     (server/url)
                   :key     "clay"
                   :title   "Clay"
                   :display :editor
                   ;; TODO: Maybe we can remove 'reveal' when fixed in Calva
                   :reveal  false
                   :info    info}]
      (if (and ide (not= browse :browser))
        (tagged-literal 'flare/html summary)
        summary))))


(comment
  (make! {:source-path       ["notebooks/scratch.clj"]
          :format [:gfm]
          :show false})
  ,
  (make! {:source-path       ["notebooks/index.clj"]
          :format [:gfm]
          :show false}))
