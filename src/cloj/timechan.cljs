(ns cloj.timechan
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop]])

  (:require
    [cljs.core.async :as ca :refer [chan <! >! put!]])
  
  )

(def ^:const target-fps 60.0)
(def ^:const min-fps 20.0)

(defn- fps-to-msecs-per-frame [fps]
  (/ 1000.0 fps))

(def target-frame-time (fps-to-msecs-per-frame target-fps))
(def min-frame-time target-frame-time)
(def max-frame-time (fps-to-msecs-per-frame min-fps))

(defn- milli-secs-to-scale [msecs]
  (let [scale (/ (* 1000.0 (/ 1.0 60.0)) msecs)]
    (max scale 1.0))) 

(defn- req-anim-frame [outchan tm]
  (do
    (js/requestAnimationFrame (partial req-anim-frame outchan))
    (put! outchan tm)
    outchan))

(defn mk-time-chan []
  (let [in-chan (req-anim-frame (chan) 0)
        out-chan (chan )]
    (go-loop [last-time nil]
             (let [this-time (<! in-chan)]
               (when-not (nil? last-time)
                 (put! out-chan [(/ (- this-time last-time) target-frame-time) this-time] ))
               (recur this-time)))
    out-chan))

