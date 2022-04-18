(ns example1
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.clay.v1.tools :as tools]
            [scicloj.clay.v1.tool.scittle :as tool.scittle]
            [scicloj.clay.v1.html.table :as table]
            [tech.v3.dataset :as tmd]
            [tablecloth.api :as tc]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind]
            [scicloj.kindly.v2.kindness :as kindness]
            [scicloj.viz.api :as viz]
            [scicloj.clay.v1.util.image :as util.image]
            [tech.v3.tensor :as tensor]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as fun]
            [tech.v3.libs.buffered-image :as bufimg]
            [clojure2d.color :as color]))

(clay/start! {:tools [tools/clerk
                      tools/portal
                      tools/scittle]})

(comment
  (clerk/clear-cache!)

  (clay/restart! {:tools [tools/clerk
                          tools/portal
                          tools/scittle]}))


;; ## Viz.clj

;; ### datasets

(-> (let [n 99]
      (tc/dataset {:preferred-language (for [i (range n)]
                                         (["clojure" "clojurescript" "babashka"]
                                          (rand-int 3)))
                   :age (for [i (range n)]
                          (rand-int 100))})))

;; ### Vega-Lite

(-> [{:x 1 :y 2}
     {:x 2 :y 4}
     {:x 3 :y 9}]
    viz/data
    (viz/type :point)
    (viz/mark-size 200)
    (viz/color :x)
    #_(assoc :BACKGROUND "#eeeee0")
    viz/viz
    #_(assoc :height 100))

;; ## Widgets with Scittle

(let [data {:day ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]
            :rain [23 24 18 25 27 28 25]}]
  (tool.scittle/show-widgets!
   ['[(fn []
        (let [*state (r/atom {})]
          (fn []
            [:div
             [echarts
              {:xAxis {:data (:day data)}
               :yAxis {}
               :series [{:type "bar"
                         :data (:rain data)}]}
              {:on {:click #(do
                              (println (-> %  js->clj pr-str))
                              (compute [:fn1
                                        (-> % .-dataIndex)]
                                       *state [:day]))}}]
             [:hr]
             [:h1 (:day @*state)]])))]]
   {:data data
    :fns {:fn1 (fn [i]
                 (-> data
                     :day
                     (get i)))}}))


;; ## Exploration -- some image processing

(def image1
  (-> "https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg"
      util.image/load-buffered-image))

(defn region-mean [image-tensor y x radius]
  (let [[height width color-components] (dtype/shape image-tensor)]
    (fun// (-> (tensor/compute-tensor [height width color-components]
                                      (fn [y1 x1 c]
                                        (if (< (+ (-> (- x1 x) (#(* % %)))
                                                  (-> (- y1 y) (#(* % %))))
                                               (* radius radius))
                                          (image-tensor y1 x1 c)
                                          0))
                                      :int32)
               dtype/clone
               (#(tensor/reduce-axis fun/sum % 0))
               (#(tensor/reduce-axis fun/sum % 0)))
           (-> (tensor/compute-tensor [height width color-components]
                                      (fn [y1 x1 c]
                                        ;; (println (+ (-> (- x1 x) (#(* % %)))
                                        ;;             (-> (- y1 y) (#(* % %)))))
                                        (if (< (+ (-> (- x1 x) (#(* % %)))
                                                  (-> (- y1 y) (#(* % %))))
                                               (* radius radius))
                                          1
                                          0))
                                      :int32)
               dtype/clone
               (#(tensor/reduce-axis fun/sum % 0))
               (#(tensor/reduce-axis fun/sum % 0))))))


(-> image1
    tensor/ensure-tensor)

(-> image1
    tensor/ensure-tensor
    (region-mean 200 200 20))

(defn tensor->img [t]
  (let [shape (dtype/shape t)
        new-img (bufimg/new-image (shape 0)
                                  (shape 1)
                                  :byte-bgr)]
    (dtype/copy! t
                 (tensor/ensure-tensor new-img))
    new-img))


(-> image1
    tensor/ensure-tensor
    (tensor/select (range 200)
                   (range 200)
                   :all)
    tensor->img)


(let [tensor1 (-> image1 tensor/ensure-tensor)
      [height width] (dtype/shape tensor1)]
  (tool.scittle/show-widgets!
   ['[(fn []
        (let [*state (r/atom {})]
          (fn []
            [:div
             [:b [:big "please click the image"]]
             [echarts
              {:graphic [{:type :image
                          :style {:image (:image data)}}]}
              {:on {:click #(compute [:colors
                                      (-> % .-event .-offsetX)
                                      (-> % .-event .-offsetY)]
                                     *state [:colors])}}]
             [:hr]
             (when-let [{:keys [colors]} @*state]
               [:div
                [:p (pr-str colors)]
                [:b [:big "point color"]]
                [:div {:style {:width (:width data)
                               :background (:point colors)}}
                 [:h1 (:point colors)]]
                [:b [:big "region average color"]]
                [:div {:style {:width (:width data)
                               :background (:region colors)}}
                 [:h1 (:region colors)]]])])))]]
   {:data {:image (-> image1
                      util.image/buffered-image->byte-array
                      util.image/byte-array->data-uri)
           :width width
           :heigh height}
    :fns {:colors (fn [x y]
                    {:x x
                     :y y
                     :point-dbg  (-> (tensor1 y x)
                                     vec)
                     :point (->> (tensor1 y x)
                                 reverse
                                 (apply color/color)
                                 color/format-hex)
                     :region (->> (region-mean tensor1 y x 20)
                                  reverse
                                  (apply color/color)
                                  color/format-hex)})}}))
