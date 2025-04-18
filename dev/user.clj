(ns user
  (:require [clojure.repl.deps :as repl-deps :refer [add-libs sync-deps]]))

(comment

  (do ;; open fresh portal
    (add-libs {'djblue/portal {:mvn/version "0.58.2"}})
    (require '[portal.api :as p])

    (do ;; clean stop
      (p/clear)
      (p/close))

    (do ;; reopen and tap
      (p/open)
      (add-tap #'p/submit)))

  ;; Visualise
  (tap> the-var-to-visualise)

  )
