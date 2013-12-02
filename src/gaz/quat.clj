(ns gaz.quat 
  (:require [gaz.math :as math]
            [gaz.util :as u]))

(defrecord Quat [^float x ^float y ^float z ^float w] )

(defn from-vec [v3]
  (let [ v3c (math/mul v3 (/ u/pi  360.0) )
        [x y z] [(:x v3c) (:y v3c) (- 0 (:y v3c))]
        [c1 c2 c3] [(u/cos y) (u/cos z) (u/cos x)]
        [s1 s2 s3] [(u/sin y) (u/sin z) (u/sin x)]
        c1c2 (* c1 c2)
        s1s2 (* s1 s2)
        ]
    (Quat.
      (+ (* c1c2 s3)  (* s1s2 c3))
      (+ (* s1 c2 c3) (* c1 s2 s3))
      (- (* c1 s2 c3) (* s1 c2 s3))
      (- (* c1c2 c3)  (* s1s2 s3))
      )))


