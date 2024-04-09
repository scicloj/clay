(ns scicloj.clay.v2.make
  (:require [scicloj.clay.v2.config :as config]
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
            [scicloj.clay.v2.files :as files]))

(defn spec->full-source-path [{:keys [base-source-path source-path]}]
  (when source-path
    (or (some-> base-source-path
                (str "/" source-path))
        source-path)))

(defn spec->source-type [{:keys [source-path]}]
  (some-> source-path
          path/path->ext))

(defn spec->ns-form [{:keys [source-type full-source-path]}]
  (when (= source-type "clj")
    (-> full-source-path
        slurp
        read/read-ns-form)))

(defn spec->full-target-path [{:keys [full-source-path
                                      source-type
                                      base-target-path
                                      format
                                      ns-form
                                      single-form
                                      single-value]}]
  (if (or single-value
          single-form
          (nil? source-type))
    (str base-target-path
         "/.clay.html")
    ;; else
    (case source-type
      "md" (str base-target-path
                "/"
                full-source-path)
      "ipynb" (str base-target-path
                   "/"
                   full-source-path)
      "clj" (path/ns->target-path base-target-path
                                  (-> ns-form
                                      second
                                      name)
                                  (str (when (-> format
                                                 second
                                                 (= :revealjs))
                                         "-revealjs")
                                       ".html")))))

(defn spec->ns-config [{:keys [ns-form]}]
  (some-> ns-form
          meta
          :clay))

(defn merge-ns-config [spec]
  (merge/deep-merge
   spec
   (spec->ns-config spec)))

(defn extract-specs [config spec]
  (let [{:as base-spec :keys [source-path]}
        (merge/deep-merge
         config spec) ; prioritize spec over global config
        ;;
        single-ns-specs (->> (if (sequential? source-path)
                               (->> source-path
                                    (map (partial assoc base-spec :source-path)))
                               [base-spec])
                             (map (fn [single-ns-spec]
                                    (-> single-ns-spec
                                        (config/add-field :full-source-path spec->full-source-path)
                                        (config/add-field :source-type spec->source-type)
                                        (config/add-field :ns-form spec->ns-form)
                                        merge-ns-config
                                        (merge/deep-merge
                                         (dissoc spec :source-path)) ; prioritize spec over the ns config
                                        (config/add-field :full-target-path spec->full-target-path)))))]

    {:main-spec (-> base-spec
                    (assoc :full-target-paths
                           (->> single-ns-specs
                                (mapv :full-target-path))))
     :single-ns-specs single-ns-specs}))


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

(defn quarto-book-config [{:as spec
                           :keys [book
                                  quarto
                                  base-target-path
                                  full-target-paths]}]
  (let [index-included? (->> full-target-paths
                             (some index-target-path?))]
    (-> quarto
        (select-keys [:format])
        (merge/deep-merge
         {:project {:type "book"}
          :book {:title (:title book)
                 :chapters (-> full-target-paths
                               (->> (map
                                     (fn [path]
                                       (-> path
                                           (string/replace
                                            (re-pattern (str "^"
                                                             base-target-path
                                                             "/"))
                                            "")
                                           (string/replace
                                            #"\.html$"
                                            ".qmd")))))
                               (cond->> (not index-included?)
                                 (cons "index.qmd")))}}))))

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
       quarto-book-config
       (write-quarto-book-config! spec))
   (-> spec
       quarto-book-index
       (write-quarto-book-index-if-needed! spec))
   (when run-quarto
     (prn [:render-book])
     (->> (shell/sh "quarto" "render")
          (shell/with-sh-dir base-target-path)
          ((juxt :err :out))
          (mapv println))
     (babashka.fs/copy-tree (str base-target-path "/_book")
                            base-target-path
                            {:replace-existing true})
     (babashka.fs/delete-tree (str base-target-path "/_book"))
     (when show
       (-> spec
           (assoc :full-target-path (str base-target-path "/index.html"))
           server/update-page!))
     [:ok])])


(defn handle-main-spec! [{:as spec
                          :keys [book]}]
  (if book
    (make-book! spec)
    [:ok]))

(defn handle-single-source-spec! [{:as spec
                                   :keys [source-type
                                          single-form
                                          single-value
                                          format
                                          full-target-path
                                          show
                                          run-quarto
                                          book]}]
  (when (or (= source-type "clj")
            single-form
            single-value)
    (try
      (files/init-target! full-target-path)
      (let [spec-with-items      (-> spec
                                     (config/add-field :items notebook/notebook-items))]
        (case (first format)
          :html (do (-> spec-with-items
                        (config/add-field :page page/html)
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
                    (println [:wrote qmd-path (time/now)])
                    (when-not book
                      (if run-quarto
                        (do (->> (shell/sh "quarto" "render" qmd-path)
                                 ((juxt :err :out))
                                 (mapv println))
                            (println [:created full-target-path (time/now)])
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
                               [full-target-path]))))))
      (catch Exception e
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
  (let [{:keys [single-form single-value]} spec
        {:keys [main-spec single-ns-specs]} (extract-specs (config/config)
                                                           spec)
        {:keys [show book base-target-path clean-up-target-dir]} main-spec]
    (when (and clean-up-target-dir
               (not (or single-form single-value)))
      (babashka.fs/delete-tree base-target-path))
    (sync-resources! main-spec)
    (when show
      (-> main-spec
          (assoc :page (-> single-ns-specs
                           first
                           (assoc :items [item/loader])
                           page/html))
          server/update-page!))
    [(->> single-ns-specs
          (mapv handle-single-source-spec!))
     (-> main-spec
         handle-main-spec!)]))
