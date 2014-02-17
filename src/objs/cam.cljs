(ns objs.cam
  (:require-macros
    [cljs.core.async.macros     :refer [go go-loop]]
    [lt.macros                  :refer [behavior]])
  
  (:require
    [cloj.jsutil                   :as jsu :refer [log strlog]]
    [lt.object                     :as object]
    [math.vec3                     :as v3]
    [objs.interpolator             :as interp]))

(behavior ::init!
          :triggers #{:init!}
          :reaction (fn [this pos interp-fn]
                      (let [i (interp/mk-interpolator pos interp-fn)]
                        (object/merge! this {:interpolator i
                                             :pos (object/raise i :get 0)
                                             }))))
(behavior ::move!
          :triggers #{:move!}
          :reaction (fn [this targ-pos start-time duration]
                      (object/raise (:interpolator @this) :move! targ-pos start-time duration)
                      (object/raise this :update!)))

(behavior ::update!
          :triggers #{:update!}
          :reaction (fn [this t]
                      (let [i       (:interpolator @this)
                            new-pos (object/raise i :get t)]
                       (object/merge! this {:pos new-pos}))))

(object/object* ::cam
                :pos (v3/mk-vec 0))

(defn mk-cam [pos interp-fun]
  (let [cam (object/create* ::cam pos interp-fun)]
    cam))

(defn simple-interpolator [t start-value target-value]
  (->>
    (v3/sub target-value start-value)
    (v3/mul-scalar t)
    (v3/add start-value)))

(defn mk-simple-cam [pos]
  (object/create* :ccam pos simple-interpolator))

