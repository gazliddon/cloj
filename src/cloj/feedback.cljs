;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Some stuff for making a feedback screen using Three js

(ns gaz.feedback
  (:require
    [gaz.three   :as THREE]
    [cloj.jsutil :as jsu]))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ends

