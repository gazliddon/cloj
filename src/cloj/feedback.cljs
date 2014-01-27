(ns gaz.feedback

  (:require-macros
    [gaz.macros              :refer [with-scene ]]
    [gaz.rendertarget :refer [with-rt]])

  (:require
    [clojure.string :as string]
    [cloj.jsutil       :as jsu]
    [gaz.renderable    :refer [RenderableProto render]]
    [gaz.layerproto    :refer [LayerProto add]]

    [gaz.layer         :as layer ]

    [gaz.math          :as math]
    [gaz.three         :refer [set-pos!]]
    [gaz.rendertarget  :as rt ]
    ))

(declare flip-feedback)



(def default-vert-shader 
  "
      varying vec2 vUv;

      void main() {
          vUv = uv;
          gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
      } " 
)

(def type-to-id {"f" "float"})

(defn gen-uniform-shader-text-entry [k v]
  (str "uniform "  (type-to-id (:type v)) " " (name k) ";"))

(defn gen-uniform-shader-text [unis]
  (map #(gen-uniform-shader-text-entry (first %) (first (rest %))) unis))

(defn mk-shader-text [effect]
  (let [uni (:uniforms effect)
        uni-text (string/join "\n" (gen-uniform-shader-text uni))]
  (str uni-text "\n" (:frag effect))))

(defn mk-shader-uniforms [effect]
  (clj->js (:uniforms effect)))

(defn mk-shader-material-map [effect]
  (let [ shader-map
        (js-obj
          "fragmentShader"  (mk-shader-text effect)
          "vertexShader" default-vert-shader
          "uniforms" (js-obj
                       "time"       (js-obj "type" "f" "value" 0.0)
                       "thisScreen" (js-obj "type" "t" "value" nil)
                       "lastScreen" (js-obj "type" "t" "value" nil)
                       ))
        uni-prop (aget shader-map "uniforms")]

    (doseq [[k v] (:uniforms effect)]
      (aset uni-prop (name k) (clj->js v))
      (aset (aget uni-prop (name k) ) "editable" true)
      )
    shader-map
    ))

(defn add-dat [gui effect material]
  (doseq [[k v] (:uniforms effect)]
    (let [min-val (:min v)
          max-val (:max v)
          value   (:value v)]
      (.add gui
            (jsu/get-prop material "uniforms" (name k))
            "value" min-val max-val))
    )
  )


(defn mk-shader-material [effect]
  (js/THREE.ShaderMaterial. (mk-shader-material-map effect)))

(defn- set-uniform [material uni val]
  (let [prop (jsu/get-prop material "uniforms" uni)]
    (aset prop "value" val)))

(defn- set-rt-uniform [material uni rt]
  (set-uniform material uni (:render-target rt)))

(defrecord FeedbackTarget [material layer front-render-target back-render-target temp-render-target]
  RenderableProto

  (render [this]
    (do
      (set-rt-uniform  material "thisScreen" front-render-target)
      (set-rt-uniform  material "lastScreen" back-render-target)
      (with-rt
        (:render-target temp-render-target)
        (render layer))

      (flip-feedback this))
    ))

(defn- flip-feedback [fb]
  (FeedbackTarget.
    (:material fb) (:layer fb)

    (:temp-render-target fb)
    (:front-render-target fb)
    (:back-render-target fb)))

(defn get-buffer [this]
  (:render-target (:front-render-target this)))

(defn render-layer [this layer]
  (with-rt (:render-target (:back-render-target this))
           (render layer)) )

(defn mk-feedback [width height effect]
  (let [material   (mk-shader-material effect)
        layer      (layer/mk-ortho-layer 1 1 {:clear true :clear-color 0x00ff00})
        front-rt   (rt/mk-render-target width height)
        back-rt    (rt/mk-render-target width height)
        temp-rt    (rt/mk-render-target width height)]

    (aset material "depthWrite" false)
    (aset material "depthTest" false)

    (add layer (js/THREE.Mesh.
                      (js/THREE.PlaneGeometry. 1 1 1 1)
                      material))

    (FeedbackTarget. material layer front-rt back-rt temp-rt)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ends

