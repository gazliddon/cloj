(ns objs.app
  (:require 
    [lt.object    :as object]
    [cloj.jsutil  :as jsu :refer [log]])

  (:require-macros
    [lt.macros :refer [behavior]])
  )

(behavior
  ::init!
  :triggers #{:init!}
  :reaction (fn [this]
              (log "reacting to an init! from an app")
              )
  )

(object/object*
  ::app
  :tags #{:app}
  :init (fn [this]
          (jsu/log (str "hello from ::app init for " this))))

(def app (object/create ::app))

