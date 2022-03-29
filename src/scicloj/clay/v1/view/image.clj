(ns scicloj.clay.v1.view.image
  (:require [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kindness :as kindness])
  (:import java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(extend-protocol kindness/Kindness
  java.awt.image.BufferedImage
  (kind [image]
    :kind/buffered-image))

(kindly/define-kind-behaviour!
  :kind/buffered-image
  {:portal.viewer (fn [^BufferedImage image]
                    (let [baos (java.io.ByteArrayOutputStream.)]
                      (ImageIO/write ^BufferedImage image "png" baos)
                      (.toByteArray baos)))})
