(ns objs.interpolator
  (:require-macros
    [lt.macros                  :refer [behavior]])
  
  (:require
    [lt.object                 :as object :refer [raise create object* merge!]]))

(defn- normalise [tm start-time duration]
  (let [my-time (- tm start-time)]
    (/ my-time duration)))

(defn- clamp [v mn mx]
  (min v (max mn v)))

(behavior ::move!
          :triggers #{:move!}
          :function (fn [this target-value start-time duration]
                      (merge! this {:start-value (raise this :get start-time)
                                    :target-value target-value
                                    :start-time start-time
                                    :duration duration})))
(behavior ::get
          :triggers #{:get}
          :reaction (fn [this t]
                      (let [{:keys [start-value
                                    target-value
                                    start-time
                                    duration
                                    value interp-fn] } @this
                            t (normalise t start-time duration )
                            t (clamp t 0 1)]
                        (if interp-fn
                          (interp-fn t start-value value)
                          start-value)
                        )))

(object/object* ::interpolator
                :tags #{:interpolator}
                :init (fn [this interp-fn start-value ]
                        (merge! this {:interp-fn interp-fn})
                        (raise this :move! start-value 0 0 )))

(defn mk-interpolator [interp-fn value]
  (create* ::interpolator interp-fn value))

