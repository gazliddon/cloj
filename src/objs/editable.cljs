(ns objs.editable
  (:require-macros
    [lt.macros                  :refer [behavior]])

  (:require
    [lt.object              :as object :refer [raise create object* merge!]]
    [cloj.jsutil            :as jsu :refer [log strlog]]))

(behavior ::add-to-ui!
          :triggers #{:add-to-ui!}
          :reaction (fn [this name]
                      (let [{:keys [ui-handle attrs] @this} ]
                        (when-not ui-handle
                          (merge! this {:ui-handle (comment "add to ui!")})))))

(behavior ::remove-from-ui!
          :triggers #{}
          :reaction (fn [this]
                      (let [{:keys [ui-handle] } @this]
                        (when ui-handle
                          (comment "remove here!")
                          (merge! this {:ui-handle nil})))))

(object* ::editable
         :init (fn [this attrs]
                 (merge! this {:attrs attrs
                               :ui-handle nil})))
