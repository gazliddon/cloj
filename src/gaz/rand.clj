(ns gaz.rand
  (:require
    [gaz.math  :as math]))

(defn rnd-rng [lo hi] (+ lo (rand (- hi lo))))

(defn rnd-norm [] (rnd-rng -0.5 0.5))

(defn rnd-vec [vmin vmax kyz]
  (map #(rnd-rng (vmin %1) (vmax %1)) kyz ))

(defn rnd-v3 [mn mx]
  (apply math/mk-vec (rnd-vec mn mx [0 1 2])))
