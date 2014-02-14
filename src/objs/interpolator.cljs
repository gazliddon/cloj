(ns objs.interpolator
  (:require-macros
    [lt.macros                  :refer [behavior]])
  
  (:require
    [lt.object                     :as object]
    ))

(defn- normalise [tm start-time duration]
  (let [my-time (- tm start-time)]
    (/ my-time duration)))

(defn- clamp [v mn mx]
  (min v (max mn v)))

(behavior ::init!
          :triggers #{:move}
          :function (fn [this interp-fn start-value target-value start-time duration]
                      (object/merge! this {:start-value (object/raise this :get)
                                           :interp-fn interp-fn})
                      (object/raise this :move! target-value start-time duration)))

(behavior ::move!
          :trigger #{:move}
          :function (fn [this target-value start-time duration]
                      (object/merge! this {:start-value (object/raise this :get start-time)
                                           :target-value target-value
                                           :start-time start-time
                                           :duration duration})
                      ))

(behavior ::get
          :triggers #{:get}
          :reaction (fn [this t]
                      (let [{:keys [start-value target-value start-time duration value interp-fn] } @this
                            t (normalise t start-time duration )
                            t (clamp t 0 1)]
                        (if interp-fn
                          (interp-fn t start-value value)
                          start-value)
                        )))

(object/object* ::interpolator
                :tags #{:interpolator}
                :target-value nil 
                :start-value nil
                :start-time 0
                :interp-fn nil 
                :duration 0)

(defn mk-interpolator [interp-fn value]
  (let [o (object/create* ::interpolator)]
    (object/raise o :init! interp-fn value value 0 0)))

