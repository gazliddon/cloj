(ns objs.effectview
  (:require-macros
    [cljs.core.async.macros     :refer [go go-loop]]
    [lt.macros                  :refer [behavior]])
  
  (:require
    [cloj.jsutil          :as jsu :refer [log strlog]]
    [lt.object            :as object]
    [math.vec3            :as v3]
    [objs.cam             :as cam]))

;; need a scene

(behavior ::update!
          :trigger #{:update! }
          )

(behavior ::render
          :trigger #{:render }
          :reaction (fn [this renderer])
          )


(object/object* ::effectview
                :tags #{:effect.view}
                :init (fn [this]
                        (object/merge! this {:effects []})
                        )
                )

(behavior ::add
          :trigger #{:add!}
          :reaction (fn [this effect]
                      (object/merge!
                        this (:effects (conj (:effects @this) effect)))))

(behavior ::info
          :trigger #{:info}
          :reaction (fn [this]
                      (count (:effects @this))))

(behavior ::remove
          :trigger #{:remove!}
          :reaction (fn [this effect]))

