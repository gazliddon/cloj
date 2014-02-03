(ns gaz.cam
  (:require 
    [math.vec3  :as math ]))

(defrecord Cam [pos lookat vel])

(defn update [cam]
  (assoc cam :pos (math/add (:pos cam) (:vel cam))))


