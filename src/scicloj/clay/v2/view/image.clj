(ns scicloj.clay.v2.view.image
  (:require [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kindness :as kindness]
            [scicloj.clay.v2.util.image :as util.image])
  (:import java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(extend-protocol kindness/Kindness
  java.awt.image.BufferedImage
  (kind [image]
    :kind/buffered-image))

#_(kindly/define-kind-behaviour!
    :kind/buffered-image
    {:scittle.viewer (fn [image]
                       [:img {:src (-> image
                                       util.image/buffered-image->byte-array
                                       util.image/byte-array->data-uri)}])})
