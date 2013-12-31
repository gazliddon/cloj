;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Some stuff for making a feedback screen using Three js

(ns gaz.feedback
  (:require
    [gaz.three   :as THREE]
    [cloj.jsutil :as jsu]))

(def THREE js/THREE)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Render Target
(defn mk-render-target [w h]
  (THREE.WebGLRenderTarget.
    w h (jsu/tojs { :minFilter THREE.LinearFilter
                :magFilter THREE.LinearFilter
                :format    THREE.RGBFormat })))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Material
(defn mk-offscr-material [material previous-rt]
  (THREE.ShaderMaterial.
    (jsu/tojs (assoc material
                     :prevScreen { :type "t" :value previous-rt }
                     :time       { :type "f" :value 0.0}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Full screen Quad
(defn mk-fsq [w h material]
  (THREE.Mesh. (THREE.PlaneGeometry. w h) material))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Scene
(defn mk-scene [fsq]
  (let [scene (THREE.Scene.)]
    (.add scene fsq)
    scene))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Camera
(defn mk-cam [w h]
  (let [wdiv2 (/ w 2)  hdiv2 (/ h 2)]
    (THREE.OrthographicCamera. (- wdiv2) wdiv2 hdiv2 (- hdiv2))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Putting it all together
(defn mk-feedback-scene [material w h rt prev-rt]
  (let [mat (mk-offscr-material material prev-rt) ]
    {:scene (mk-scene (mk-fsq w h mat))
     :rt rt}))

(defn mk-feedback [material w h]
  (let [fbfunc (partial mk-feedback-scene material w h)
        rt0   (mk-render-target w h)
        rt1   (mk-render-target w h)]
    {:cam     (mk-cam w h)
     :scene-0 (fbfunc rt0 rt1)
     :scene-1 (fbfunc rt1 rt0) }))

(defn flip-feedback [fb]
  (assoc fb :scene-0 (:scene-1 fb) :scene-1 (:scene-0 fb)))

(defn render-feedback
  "TODO!"
  [])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;0;
(defprotocol Renderable
  (render [this scene camera ])
  (get-rt [this])
  (get-width [this])
  (get-height [this])
  (get-aspect-ratio [this]))

(extend-protocol Renderable
  object
  (render [obj scene camera]
    (.render (get-rt obj) scene camera))
  (get-width [obj] (get-width (get-rt obj)))
  (get-height [obj] (get-height (get-rt obj)))
  (get-aspect-ratio [obj] (/ (get-width obj) (get-height obj))))

;; Rendertaget stuff
(defrecord RenderTarget [w h rt]
  Renderable
  (get-rt [_] rt)
  (get-width [_] w)
  (get-height [_] h) )

(defn mk-render-target [w h]
  (let [rt (THREE.WebGLRenderTarget.
             w h (jsu/tojs {:minFilter THREE.LinearFilter
                            :magFilter THREE.LinearFilter
                            :format    THREE.RGBFormat }))]
    (->RenderTarget w h rt)))


;; Feedback Buffer
(defrecord FeedbackBuffer [dest-rt source-rt]
  Renderable
  (render [_ scene camera]
    (.render dest-rt scene camera))
  (get-rt [_] (get-rt dest-rt)))

(defrecord Feedback [material front-buffer back-buffer cam scene]
  Renderable
  (get-rt [_] (get-rt front-buffer)))

(defn mk-feedback [w h material]
  (let [front-rt (mk-render-target w h)
        back-rt  (mk-render-target w h)]
    (->Feedback
      material
      (->FeedbackBuffer back-rt front-rt)
      (->FeedbackBuffer back-rt front-rt)
      nil
      nil)))

(defn flip-fb [fb]
  (-> fb
    (assoc :front-buffer (:back-buffer  fb))
    (assoc :back-buffer  (:front-buffer fb))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ends

