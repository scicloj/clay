(ns scicloj.clay.v2.doc
  (:require [scicloj.clay.v2.prepare :as prepare]
            [scicloj.clay.v2.server :as server]
            [scicloj.clay.v2.widget :as widget]
            [scicloj.clay.v2.read]
            [scicloj.clay.v2.path :as path]
            [clojure.string :as string]
            [nextjournal.markdown :as md]
            [nextjournal.markdown.transform :as md.transform]
            [hiccup.core :as hiccup]
            [clojure.walk :as walk]))

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
    (widget/mark-plain-html
     [:div
      (when relative-file-path
        [:code
         [:small
          [:small
           "source: "
           (if git-url
             [:a {:href git-url} relative-file-path]
             relative-file-path)]]])])))

(comment
  (-> "notebooks/index.clj"
      (gen-doc {})
      (->> (map (juxt meta identity)))))

(def separator
  (widget/mark-plain-html
   [:div {:style
          {:height "2px"
           :width "100%"
           :background-color "grey"}}]))

(defn gen-doc
  ([path {:keys [hide-info-line?
                 hide-code? hide-nils? hide-vars?
                 title toc?]}]
   (-> path
       slurp
       scicloj.clay.v2.read/->safe-notes
       (->> (map (fn [{:as note
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
                                   deref-if-needed))))))
            (mapcat (fn [{:as note
                          :keys [comment? code form value]}]
                      (if comment?
                        [(let [markdown
                               (-> code
                                   (string/split #"\n")
                                   (->> (map #(-> %
                                                  (string/replace
                                                   #"^;+\s*" "")
                                                  (string/replace
                                                   #"^#" "\n#")))
                                        (string/join "\n")))]
                           (->> markdown
                                md/parse
                                md.transform/->hiccup
                                ;; TODO: How to handle these better?
                                ;; (:<> does not work in plain hiccup)
                                (walk/postwalk-replace {:<> :div})
                                widget/mark-plain-html
                                (#(vary-meta
                                   %
                                   assoc
                                   :clay/original-markdown markdown))))]
                        [(-> {:value (-> note
                                         :code
                                         vector)
                              :kind :kind/code}
                             prepare/prepare-or-pprint
                             (#(vary-meta
                                %
                                assoc
                                :clay/original-code (-> note
                                                        :code)
                                :clay/hide-code? (or hide-code?
                                                     (-> form meta :kindly/hide-code?)
                                                     (-> value meta :kindly/hide-code?)))))
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
                               widget/in-div ; TODO: was this needed?
                               ((fn [ctx]
                                  (if (-> note :value meta :kindly/kind (= :kind/md))
                                    (-> ctx
                                        (vary-meta
                                         assoc
                                         :clay/original-markdown
                                         (->> note
                                              :value
                                              (string/join "\n"))))
                                    ctx)))
                               ((fn [ctx]
                                  (if (-> note :value meta :kindly/kind (= :kind/code))
                                    (-> ctx
                                        (vary-meta
                                         assoc
                                         :clay/original-code
                                         (->> note
                                              :value
                                              (string/join "\n"))))
                                    ctx)))))])))
            (filter some?))
       ((fn [items]
          (if hide-info-line?
            items
            (let [il (info-line path)]
              (concat #_[il
                         separator]
                      items
                      [separator
                       il])))))
       doall)))
