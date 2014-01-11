(ns gaz.feedback

  (:require-macros
    [gaz.macros              :refer [with-scene with-rt]])

  (:require
    [cloj.jsutil       :as jsu]
    [gaz.renderable    :refer [RenderableProto render]]

    [gaz.layer         :as layer
     :refer [LayerProto get-scene]]

    [gaz.math          :as math]
    [gaz.three         :refer [set-pos!]]
    [gaz.rendertarget  :as rt :refer [mk-render-target]]
    ))

(defrecord FeedbackTarget [front-render-target back-render-target ]

  RenderableProto

  (render [this]
    (render front-render-target)
    ))

(defn- add-plane-obj! [dest-target source-target]
  (let [{:keys [width height]} dest-target
        geo (THREE.PlaneGeometry. width height)
        msh (THREE.Mesh. geo (:material source-target)) ]

    (layer/add dest-target msh)))

(defn mk-feedback [width height]
  (let [[wd2 hd2] [(/ width 2.0) (/ height 2.0)]
        cam   (js/THREE.OrthographicCamera.
                (- wd2) wd2 hd2 (- hd2) 0.001 1000)
        front (mk-render-target width height)
        back  (mk-render-target width height) ]

    (set-pos! cam (array  0 0 1))
    (add-plane-obj! front back)
    (add-plane-obj! back front)

    (FeedbackTarget.
      (assoc front :cam cam)
      (assoc back  :cam cam))
    ))

(defn get-back-screen [fb] (:back fb))
(defn get-back-rt [fb] (:render-target (get-back-screen fb)))
(defn flip-feedback [fb]
  (FeedbackTarget. (:back fb) (:front fb)))

(defn render-layer-to-feedback [fb layer]
  (with-rt
    (get-back-rt fb)
    (layer/render layer)))

