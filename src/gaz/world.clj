(ns gaz.world
  (:require [math.vec3  :as math ]))

(defn- mk-pos [x y z] (math/vec3 x y z))

(defn make-coords [w h] (for [ x (range 0 w) y (range 0 h)] (mk-pos x y 0)))
(defn mk-tile [t p] {:tile t :pos p})

(def world-width 10)
(def world-height 10)
(def world-coords (make-coords world-width world-height))
(def world-map (map #(mk-tile 1 %) world-coords ))

(defn iterate-world [f world] (doseq [k world] (f k)))


