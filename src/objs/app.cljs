(ns objs.app
  (:require 
    [lt.object    :as object]
    [cloj.jsutil  :as jsu ]
    )

  (:require-macros
    [lt.macros :refer [behavior]])
  )

(object/object* ::app
         ::init  (fn [this]
                   (jsu/log (str "hello from ::app init for " this))))


(def app (object/create ::app))

