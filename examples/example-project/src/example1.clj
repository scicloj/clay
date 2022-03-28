(ns example1
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind])
  (:import javax.imageio.ImageIO
           java.net.URL))

(clay/start)
(clay/setup-clerk)

(+ 1 2)

(defonce clay-image
  (-> "https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg"
      (URL.)
      (ImageIO/read)))

clay-image

(delay
  (Thread/sleep 500)
  (+ 1 2))

(-> 2
    (+ 3)
    (clay/check = 4))

(-> 2
    (+ 3)
    (clay/check = 5))
