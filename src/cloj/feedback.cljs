(ns gaz.feedback

  (:require-macros
    [gaz.macros              :refer [with-scene ]]
    [render.rendertarget :refer [with-rt]])

  (:require
    [clojure.string :as string]
    [cloj.jsutil       :as jsu]
    [render.renderable    :refer [RenderableProto render]]
    [gaz.layerproto    :refer [LayerProto add]]

    [gaz.layer         :as layer ]

    [gaz.math          :as math]
    [gaz.three         :refer [set-pos!]]
    [render.rendertarget  :as rt ]

    [render.shader     :as shader]

    [ui.editable       :as editable]
    ))

(declare flip-feedback)


(defn- set-uniform [material uni val]
  (let [prop (get-in material [ "uniforms" uni ])]
    (aset prop "value" val)))

(defn- set-rt-uniform [shader uni rt]
  (set-uniform (:material shader) uni (:render-target rt)))

(defrecord FeedbackTarget [shader layer front-render-target back-render-target temp-render-target]


  editable/UIEditable

  (add-to-dat [_ dat]
    (let [folder (.addFolder dat "shader") ]
      (editable/add-to-dat shader folder)))
  
  RenderableProto

  (render [this]
    (do
      (set-rt-uniform shader "thisScreen" front-render-target)
      (set-rt-uniform shader "lastScreen" back-render-target)
      (with-rt
        (:render-target temp-render-target)
        (render layer))

      )
    ))

(defn flip [fb]
  (FeedbackTarget.
    (:shader fb) (:layer fb)
    (:temp-render-target fb)
    (:front-render-target fb)
    (:back-render-target fb)))

(defn get-buffer [this]
  (:render-target (:front-render-target this)))

(defn render-layer [this layer]
  (with-rt (:render-target (:back-render-target this))
           (render layer)) )

(defn mk-feedback [width height effect]
  (let [shader     (shader/mk-material effect)
        material   (:material shader)
        layer      (layer/mk-ortho-layer 1 1 {:clear true :clear-color 0x00ff00})
        front-rt   (rt/mk-render-target width height)
        back-rt    (rt/mk-render-target width height)
        temp-rt    (rt/mk-render-target width height)]

    (aset material "depthWrite" false)
    (aset material "depthTest" false)

    (add layer (js/THREE.Mesh.
                 (js/THREE.PlaneGeometry. 1 1 1 1)
                 material))

    (FeedbackTarget. shader layer front-rt back-rt temp-rt)))






;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ends

