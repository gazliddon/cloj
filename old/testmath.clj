(ns gaz.testmath
  (:require [gaz.math  :as math]
            [gaz.util :as util]))

(defn myabs [v]
  (if (< v 0)
    (- 0 v)
    v))

(def test-math {:abs myabs :check 10})

(do
  (math/init! test-math)
  (println math/math-atom)
  (println  ((:abs @math/math-atom) -1))
  (println (math/abs -1)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ends
