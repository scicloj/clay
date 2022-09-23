(ns scicloj.clay.v2.tool.scittle.doc
  (:require [scicloj.kindly.v2.kind :as kind]
            ;; [nextjournal.clerk :as clerk]
            [nextjournal.markdown.transform]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.clay.v2.view :as view]
            [scicloj.clay.v2.tool.scittle.view :as scittle.view]
            [scicloj.clay.v2.tool.scittle.server :as server]
            [scicloj.clay.v2.tool.scittle.widget :as widget]
            [scicloj.clay.v2.read]
            [taoensso.nippy :as nippy]
            [clojure.string :as string]
            [nextjournal.markdown :as md]
            [nextjournal.markdown.transform :as md.transform]))


(defn show-doc!
  ([path]
   (show-doc! path nil))
  ([path {:keys [hide-code? hide-nils? hide-vars? hide-toc?
                 title toc?]}]
   (-> path
       slurp
       scicloj.clay.v2.read/->safe-notes
       (->> (map (fn [note]
                   (if (:comment? note)
                     note
                     (assoc
                      note
                      :value (-> note
                                 :source
                                 read-string
                                 eval)))))
            (map (fn [note]
                   (if (:comment? note)
                     (-> note
                         :source
                         (string/split #"\n")
                         (->> (map #(string/replace % #"^;*" ""))
                              (string/join "\n"))
                         md/parse
                         md.transform/->hiccup)
                     (-> note
                         (select-keys [:value :form])
                         view/prep-for-show
                         ((juxt :value-to-show :kind-override))
                         (->> (apply scittle.view/prepare)))))))
       doall
       (#(server/show-widgets! % {:title path
                                  :toc? toc?})))))


(comment
  (show-doc! "notebooks/intro.clj"))
