(ns objs.geo
  (:require-macros
    [cljs.core.async.macros      :refer [go go-loop]]
    [lt.macros                   :refer [behavior]])
  
  (:require
    [lt.object                 :as object :refer [raise create object* merge!]]
    [gaz.three                 :as three]
    [cloj.jsutil               :as jsu :refer [log strlog]]
    [ui.attrs                  :as attrs :refer [v3 fl]]
    [math.vec3                 :as v3]
    [math.constants            :refer [pi tau]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generic
(defn iterate-items [items fun]
  (let [step (/ 1.0 (count items))]
    (loop [r-items items i 0]
      (when (seq r-items)
        (fun (first r-items) i (* i step))
        (recur (rest r-items) (inc i))))) )

(defn attrs-to-map [attrs mp]
  (reduce (fn [r [k v]] (assoc r k @(:value  v))) {}  attrs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generic
(behavior ::add-to-scene!
          :triggers #{:add!}
          :reaction (fn [this scene]
                      (let [cur @this]
                        (doseq [c (:objs @this)]
                          (.add scene c)))))

(behavior ::remove-from-scene!
          :triggers #{:remove!}
          :reaction (fn [this scene]
                      (assert false)))

(behavior ::update!
          :triggers #{:update!}
          :reaction (fn [this tm]
                      (let [{:keys [attrs objs update-fn!]} @this]
                        (update-fn! objs (attrs-to-map attrs {}) tm ))))
(object/object* ::geo
                :tags #{:geo}
                :init (fn [this attrs objs update-fn!]
                        (merge! this {:attrs attrs
                                      :objs objs 
                                      :update-fn! update-fn!})))

(defn mk-geo [scene attrs objs update-fn!]
  (let [o (create ::geo attrs objs update-fn!)]
    (raise o :add! scene)
    o))

