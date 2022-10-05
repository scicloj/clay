(ns scicloj.clay.v2.view.image
  (:require [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kindness :as kindness]
            [scicloj.clay.v2.util.image :as util.image]
            [scicloj.clay.v2.tool.scittle.view :as scittle.view])
  (:import java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(extend-protocol kindness/Kindness
  java.awt.image.BufferedImage
  (kind [image]
    :kind/buffered-image))

(scittle.view/add-viewer!
 :kind/buffered-image
 (fn [image]
   [:img {:style {:width "100%"}
          :src (-> image
                   util.image/buffered-image->byte-array
                   util.image/byte-array->data-uri)}]))
