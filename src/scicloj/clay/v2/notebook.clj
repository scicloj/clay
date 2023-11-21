(ns scicloj.clay.v2.notebook
  (:require
   [clojure.string :as string]
   [hiccup.core :as hiccup]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.util.path :as path]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.prepare :as prepare]
   [scicloj.clay.v2.read :as read]
   [scicloj.clay.v2.config :as config]
   [scicloj.clay.v2.files :as files]))

(defn deref-if-needed [v]
  (if (delay? v)
    (let [_ (println "deref ...")
          dv @v
          _ (println "done.")]
      dv)
    v))

(def hidden-form-starters
  #{'ns 'comment
    'def 'defonce 'defn 'defmacro
    'defrecord 'defprotocol 'deftype
    'extend-protocol 'extend})

(defn info-line [absolute-file-path]
  (let [relative-file-path (path/path-relative-to-repo
                            absolute-file-path)]
    (item/info-line {:path relative-file-path
                     :url (some-> (config/config)
                                  :remote-repo
                                  (path/file-git-url relative-file-path))})))

(defn complete-note [{:as note
                      :keys [comment? code form]}]
  (if comment?
    note
    (assoc
     note
     :value (if form
              (-> form
                  eval
                  deref-if-needed)
              (-> code
                  read-string
                  eval
                  deref-if-needed)))))

(defn comment->item [comment]
  (-> comment
      (string/split #"\n")
      (->> (map #(-> %
                     (string/replace
                      #"^;+\s*" "")
                     (string/replace
                      #"^#" "\n#")))
           (string/join "\n"))
      item/md))

(defn note-to-items [{:as note
                      :keys [comment? code form value]}
                     {:keys [hide-code? hide-nils? hide-vars?]}]
  (if (and comment? code)
    [(comment->item code)]
    [;; code
     (when-not (or hide-code?
                   (-> form meta :kindly/hide-code?)
                   (-> value meta :kindly/hide-code?)
                   (nil? code))
       (item/source-clojure code))
     ;; value
     (when-not (or
                (and (sequential? form)
                     (-> form first hidden-form-starters))
                (-> note :form meta :kind/hidden)
                (and hide-nils? (nil? value))
                (and hide-vars? (var? value)))
       (-> note
           (select-keys [:value :code :form :target-path])
           (update :value deref-if-needed)
           prepare/prepare-or-pprint
           ;; in-div ; TODO: is this needed?
           ))]))

(defn add-info-line [items path {:keys [hide-info-line?]}]
  (if hide-info-line?
    items
    (let [il (info-line path)]
      (concat #_[il
                 item/separator]
              items
              [item/separator
               il]))))

(defn notebook-items
  ([path {:as options
          :keys [hide-info-line?
                 hide-code? hide-nils? hide-vars?
                 title toc?
                 target-path
                 single-form]}]
   (files/init-target! target-path)
   (let [code (slurp path)
         notes  (if single-form
                  [{:form (read/read-ns-form code)}
                   {:form single-form}]
                  (read/->safe-notes code))]
     (-> notes
         (->> (mapcat (fn [note]
                        (-> note
                            complete-note
                            (assoc :target-path target-path)
                            (note-to-items options))))
              (remove nil?))
         (add-info-line path options)
         doall))))

(comment
  (-> "notebooks/scratch.clj"
      (notebook-items {:target-path "docs/scratch.html"}))

  (-> "notebooks/scratch.clj"
      (notebook-items {:target-path "docs/scratch.html"
                       :single-form '(+ 1 2)}))
  )
