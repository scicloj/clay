(ns scicloj.clay.v1.view.image
  (:require [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kindness :as kindness]
            [scicloj.clay.v1.util.image :as util.image])
  (:import java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(extend-protocol kindness/Kindness
  java.awt.image.BufferedImage
  (kind [image]
    :kind/buffered-image))

(kindly/define-kind-behaviour!
  :kind/buffered-image
  {:portal.viewer util.image/buffered-image->byte-array
   :scittle.viewer (fn [image]
                     [:img {:src (-> image
                                     util.image/buffered-image->byte-array
                                     util.image/byte-array->data-uri)}])})
