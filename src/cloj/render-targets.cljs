(ns gaz.render-targets
  (:require
    [gaz.three   :as THREE]
    [cloj.jsutil :as jsu]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;0;
;; Rendertaget stuff
(defrecord RenderTarget [w h rt])

(defn mk-render-target [w h]
  (let [rt (THREE.WebGLRenderTarget.
             w h (clj->js {:minFilter THREE.LinearFilter
                            :magFilter THREE.LinearFilter
                            :format    THREE.RGBFormat }))]
    (->RenderTarget w h rt)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;0;
;; Feedback Buffer
(defrecord FeedbackBuffer [dest-rt source-rt] )
(defn mk-feedbackbuffer  [dest-rt source-rt]
  (->FeedbackBuffer dest-rt source-rt))

(defrecord FeedbackScene [material front-buffer back-buffer cam scene] )

(defn mk-feedback-scene [w h material]
  (let [front-rt (mk-render-target w h)
        back-rt  (mk-render-target w h)]
    (->FeedbackScene
      material
      (mk-feedbackbuffer back-rt front-rt)
      (mk-feedbackbuffer  back-rt front-rt)
      nil
      nil)))

(defn flip-feedback [fb]
  (-> fb
    (assoc :front-buffer (:back-buffer  fb))
    (assoc :back-buffer  (:front-buffer fb))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;0;
(defprotocol Renderable
  (render [this scene camera ])
  (get-rt [this])
  (get-width [this])
  (get-height [this])
  (get-aspect-ratio [this]))

(extend-protocol Renderable

  RenderTarget
  (get-rt [obj] (:rt obj))
  (get-width  [obj] (:w obj))
  (get-height [obj] (:h obj))

  FeedbackScene
  (get-rt [obj] (:front-buffer obj))
  (get-width [obj]  (get-width  (get-rt obj)))
  (get-height [obj] (get-height (get-rt obj)))

  FeedbackBuffer
  (get-rt [obj] (:dest-rt obj))
  (get-width [obj]  (get-width  (get-rt obj)))
  (get-height [obj] (get-height (get-rt obj)))

  )

(defn get-aspect-ratio [obj] (/ (get-width obj) (get-height obj)))

