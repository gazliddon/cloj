(ns gaz.control
  (:require 
    [gaz.util :as util]
    [gaz.math :as math ]))

(def pvel 0.01)
(def nvel (- 0 pvel))

(def max-vel (math/mk-vec 1 1 1 ))
(def min-vel (math/neg max-vel))

(defn clamp-vel [v-in]
  (math/clamp v-in min-vel max-vel))

(def key-to-symbol {\W :up
                    \A :left
                    \S :down
                    \D :right
                    \F :fire
                    \P :rise
                    \L :fall })

(def symbol-to-vel {:left  (math/mk-vec nvel 0 0)
                    :right (math/mk-vec pvel 0 0)
                    :up    (math/mk-vec 0 pvel 0)
                    :down  (math/mk-vec 0 nvel 0)
                    :rise  (math/mk-vec 0 0 pvel)
                    :fall  (math/mk-vec 0 0 nvel)})

(def key-to-vel (comp symbol-to-vel key-to-symbol))

(defn- keypresses-to-vel
  "Turn a list of key press values into a final velocity"
  [xs]
  (->> xs
    (map key-to-vel)
    (util/filter-out-nil)
    (reduce math/add math/zero)))


;; Pipe keys through to control and get a velocity vector
(defn keys-to-vel
  "Return a new velocity adjusted by the controls"
  [curr-vel curr-keys]
  (do 
    (->> curr-keys
      (keypresses-to-vel)
      (math/add curr-vel)
      (clamp-vel)
      (math/mul-scalar 0.95))))

