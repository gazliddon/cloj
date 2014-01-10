k; Adapter that takes an input of current time
;; and spits out an event with the the delta time between the last two frames

(ns gaz.frame
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [gaz.util :as util]
    [cljs.core.async :as ca]))

(def max-frame-time 33.0)

(defn mk-chan [in-chan out-chan]
  (go-loop [last-val nil ]
           (let [this-val (ca/<! in-chan)]

             (if (util/not-nil? last-val)
               (ca/>! out-chan (min max-frame-time  (- this-val last-val))))

             (recur this-val))))
