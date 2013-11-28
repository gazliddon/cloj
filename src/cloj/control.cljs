(ns gaz.control
  (:require 
    [gaz.util :as util]
    [gaz.math :as math ]))

(def pvel 0.01)
(def nvel (- 0 pvel))

(def max-vel (math/vec3 1 1 1 ))
(def min-vel (math/neg max-vel))

(defn clamp-vel [v-in] (math/clamp v-in min-vel max-vel))

(def key-to-symbol {\W :up
                    \A :left
                    \S :down
                    \D :right
                    \F :fire
                    \P :rise
                    \L :fall })

(def symbol-to-vel {:left  (math/vec3 nvel 0 0)
                    :right (math/vec3 pvel 0 0)
                    :up    (math/vec3 0 pvel 0)
                    :down  (math/vec3 0 nvel 0)
                    :rise  (math/vec3 0 0 pvel)
                    :fall  (math/vec3 0 0 nvel)})

(def key-to-vel (comp symbol-to-vel key-to-symbol))

(defn keys-to-vel
  "Turn a list of key press values into a final velocity"
  [xs]
  (->> xs
    (map key-to-vel)
    (util/filter-out-nil)
    (reduce math/add math/zero)))

