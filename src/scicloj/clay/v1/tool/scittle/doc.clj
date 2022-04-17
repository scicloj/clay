(ns scicloj.clay.v1.tool.scittle.doc
  (:require [scicloj.kindly.v2.kind :as kind]
            [nextjournal.clerk :as clerk]
            [scicloj.clay.v1.tool.scittle.view :as view]
            [scicloj.clay.v1.tool.scittle.server :as server]
            [scicloj.clay.v1.tool.scittle.widget :as widget]
            [taoensso.nippy :as nippy]))

(defn clerk-eval
  [file]
  (reset! clerk/!last-file file)
  (let [doc (clerk/parse-file file)
        {:keys [blob->result]} @nextjournal.clerk.webserver/!doc]
    (clerk/+eval-results blob->result doc)))

(defn show-doc!
  ([path]
   (show-doc! path nil))
  ([path {:keys [hide-code?]}]
   (->> path
        clerk-eval
        :blocks
        (mapcat (fn [block]
                  (case (:type block)
                    :code [(when-not hide-code?
                             (-> block
                                 :text
                                 widget/code))
                           (-> block
                               :result
                               :nextjournal/value
                               ((fn [v]
                                  (-> v
                                      :nextjournal.clerk/var-from-def
                                      (or v))))
                               view/prepare)]
                    :markdown [(-> block
                                   :text
                                   vector
                                   kind/md
                                   view/prepare)])))
        (filter (complement :nippy/unthawable))
        server/show-widgets!)))


(comment
  (show-doc! "notebooks/intro.clj")
  )