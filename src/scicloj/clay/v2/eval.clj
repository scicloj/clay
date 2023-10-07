(ns scicloj.clay.v2.eval
  (:require
   [clojure.string :as string]
   [clojure.walk :as walk]
   [hiccup.core :as hiccup]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.path :as path]
   [scicloj.clay.v2.prepare :as prepare]
   [scicloj.clay.v2.read]
   [scicloj.clay.v2.server :as server]))

(defn deref-if-needed [v]
  (if (delay? v)
    (let [_ (println "deref ...")
          dv @v
          _ (println "done.")]
      dv)
    v))

(def hidden-form-starters
  #{'ns 'comment 'defn 'def 'defmacro 'defrecord 'defprotocol 'deftype})

(defn info-line [absolute-file-path]
  (let [relative-file-path (path/path-relative-to-repo
                            absolute-file-path)
        git-url (some-> (server/options)
                        :remote-repo
                        (path/file-git-url relative-file-path))]
    [:div
     (when relative-file-path
       [:code
        [:small
         [:small
          "source: "
          (if git-url
            [:a {:href git-url} relative-file-path]
            relative-file-path)]]])]))

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
  (if comment?
    [(comment->item code)]
    [;; code
     (when-not (or hide-code?
                   (-> form meta :kindly/hide-code?)
                   (-> value meta :kindly/hide-code?))
       (item/source-clojure code))
     ;; value
     (when-not (or
                (and (sequential? form)
                     (-> form first hidden-form-starters))
                (-> note :form meta :kind/hidden)
                (and hide-nils? (nil? value))
                (and hide-vars? (var? value)))
       (-> note
           (select-keys [:value :code :form])
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

(defn gen-doc
  ([path {:as options
          :keys [hide-info-line?
                 hide-code? hide-nils? hide-vars?
                 title toc?]}]
   (-> path
       slurp
       scicloj.clay.v2.read/->safe-notes
       (->> (map complete-note)
            (map #(note-to-items % options))
            (remove nil?))
       (add-info-line path options)
       doall)))

(comment
  (-> "notebooks/index.clj"
      (gen-doc {})))
