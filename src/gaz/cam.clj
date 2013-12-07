(ns gaz.cam
  (:require 
    [gaz.math :as math ]))

(defrecord Cam [pos lookat vel])

(defn update [cam]
  (assoc cam :pos (math/add (:pos cam) (:vel cam))))


