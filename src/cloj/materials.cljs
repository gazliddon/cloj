(ns gaz.materials
  (:require
    [gaz.math2 :as math ]))

(defn mk-flat [col]
  (THREE.MeshPhongMaterial.
    (clj->js {"color" col
              "shininess" 100})))

(defn mk-textured [col uri]
  (THREE.MeshPhongMaterial.
    (clj->js {"color" col
              "shininess" 100
              "map" (.loadTexture THREE.ImageUtils uri)})))

(defn mk-shader [vs fs uniforms])
(comment 
  (def red-flat   (mk-flat 0xff0000))
  (def green-flat (mk-flat 0x00ff00))
  (def blue-flat  (mk-flat 0x0000ff))
  (def textured   (mk-textured 0x808080 "feedback.jpg")))
