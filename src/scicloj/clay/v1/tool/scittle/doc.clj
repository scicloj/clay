(ns scicloj.clay.v1.tool.scittle.doc
  (:require [scicloj.kindly.v2.kind :as kind]
            [nextjournal.clerk :as clerk]
            [nextjournal.markdown.transform]
            [scicloj.clay.v1.view]
            [scicloj.clay.v1.tool.scittle.view :as view]
            [scicloj.clay.v1.tool.scittle.server :as server]
            [scicloj.clay.v1.tool.scittle.widget :as widget]
            [taoensso.nippy :as nippy]
            [clojure.string :as string]))

(defn clerk-eval
  [file]
  (reset! clerk/!last-file file)
  (let [doc (clerk/parse-file file)
        {:keys [blob->result]} @nextjournal.clerk.webserver/!doc]
    (clerk/+eval-results blob->result doc)))

(defn show-doc!
  ([path]
   (show-doc! path nil))
  ([path {:keys [hide-code? hide-nils? hide-vars? hide-toc?
                 title]}]
   (cond->> path
     true clerk-eval
     true :blocks
     true (mapcat (fn [block]
                    (case (:type block)
                      :code (when-not
                                (-> block
                                    :form
                                    meta
                                    :kind/hidden)
                              [(when-not hide-code?
                                 (-> block
                                     :text
                                     vector
                                     kind/code))
                               (-> block
                                   :result
                                   :nextjournal/value
                                   ((fn [v]
                                      (-> v
                                          :nextjournal.clerk/var-from-def
                                          (or v))))
                                   scicloj.clay.v1.view/deref-if-needed)])
                      :markdown [(some-> block
                                         :doc
                                         nextjournal.markdown.transform/->hiccup
                                         kind/hiccup
                                         widget/mark-plain-html)])))
     hide-nils? (filter some?)
     hide-vars? (filter (complement var?))
     true (filter (complement :nippy/unthawable))
     true (map view/prepare)
     true (#(server/show-widgets! % {:title (or (-> path
                                                    (string/split #"/")
                                                    last
                                                    (string/split #"\.")
                                                    first))
                                     :toc? (not hide-toc?)})))))

(comment
  (show-doc! "notebooks/intro.clj"))
