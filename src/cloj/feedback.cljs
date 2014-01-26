(ns gaz.feedback

  (:require-macros
    [gaz.macros              :refer [with-scene ]]
    [gaz.rendertarget :refer [with-rt]])

  (:require
    [cloj.jsutil       :as jsu]
    [gaz.renderable    :refer [RenderableProto render]]
    [gaz.layerproto    :refer [LayerProto add]]

    [gaz.layer         :as layer ]

    [gaz.math          :as math]
    [gaz.three         :refer [set-pos!]]
    [gaz.rendertarget  :as rt ]
    ))

(declare flip-feedback)

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
    (:back-render-target fb)
    ))

(def default-vert-shader 
  "
      varying vec2 vUv;

      void main() {
          vUv = uv;
          gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
      } ")

(def default-frag-shader
  "
      varying vec2       vUv;
      uniform sampler2D  lastScreen;
      uniform sampler2D  thisScreen;

      uniform float      time;

      uniform float u_x_scale;
      uniform float u_y_scale;
      uniform float u_lpix_scale;
      uniform float u_mix;
      uniform float u_fpix_scale;

      vec2 scaleIt (vec2 v, vec2 scale) {
        vec2 offset= vec2(0.5,0.5);
        vec2 tmp = v - offset;
        tmp = tmp * scale;
        tmp = tmp + offset;
        return tmp;
      }
  
      vec4 frct(vec4 v)
      {
          return (v - floor(v));
      }
  
      void main() {
          vec2 thisUv = vUv;
          vec2 lastUv = scaleIt(vUv, vec2(u_x_scale, u_y_scale));
          vec4 lastPix = texture2D (lastScreen, lastUv);
          vec4 thisPix = texture2D (thisScreen, thisUv);
          vec4 finalPix = (thisPix+ (lastPix * u_lpix_scale));
          
          finalPix = mix(thisPix, frct (finalPix), u_mix);

          finalPix = u_fpix_scale * finalPix * lastPix;

          gl_FragColor = finalPix ; 
      } ")

(def default-material
  (js/THREE.ShaderMaterial. 
    (js-obj
      "fragmentShader" default-frag-shader
      "vertexShader"   default-vert-shader
      "uniforms"       (js-obj
                         "time" (js-obj "type" "f" "value" 0.0)
                         "thisScreen" (js-obj "type" "t" "value" nil)
                         "lastScreen" (js-obj "type" "t" "value" nil)

                         "u_x_scale"       (js-obj "type" "f" "value" 0.99)
                         "u_y_scale"       (js-obj "type" "f" "value" 0.99)
                         "u_lpix_scale"  (js-obj "type" "f" "value" 1.5)
                         "u_mix"         (js-obj "type" "f" "value" 0.1)
                         "u_fpix_scale"  (js-obj "type" "f" "value" 2.0)
                         ))))

(defn get-buffer [this]
  (:render-target (:front-render-target this)))

(defn render-layer [this layer]
  (with-rt (:render-target (:back-render-target this))
           (render layer)) )


(defn mk-feedback [width height & [material']]
  (let [material  default-material 
        layer      (layer/mk-ortho-layer 1 1 {:clear true :clear-color 0x00ff00})
        layer'      (layer/mk-perspective-layer 1 1 20 (array 0 0 1) {:clear true :clear-color 0x00ff00})
        front-rt   (rt/mk-render-target width height)
        back-rt    (rt/mk-render-target width height)
        temp-rt    (rt/mk-render-target width height)]

    (aset material "depthWrite" false)
    (aset material "depthTest" false)

    (jsu/log material)
  
    (add layer (js/THREE.Mesh.
                      (js/THREE.PlaneGeometry. 1 1 1 1)
                      material))

    (FeedbackTarget. material layer front-rt back-rt temp-rt)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ends



