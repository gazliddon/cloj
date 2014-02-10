(ns objs.settings
  (:require-macros
    [cljs.core.async.macros     :refer [go go-loop]]
    [lt.macros                  :refer [behavior]])
  
  (:require
    [cljs.reader                  :as reader]
    [cljs.core.async              :as ca :refer [chan <! >! close!]]
    [file.loader                  :as loader ]
    [cloj.jsutil                  :as jsu :refer [log]]
    [lt.object                    :as object]
    ))

(defn safe-read [s file]
  (when s
    (try
      (reader/read-string s)
      (catch js/global.Error e
        (log (str "Invalid settings file: " file "\n" e))
        nil)
      (catch js/Error e
        (log (str "Invalid settings file: " file "\n" e))
        nil))))

(behavior ::load-settings
          :triggers #{:settings.load}
          :reaction (fn [this file]
                      (object/raise this :file.load file)))

(behavior ::settings-loaded
          :triggers #{file.loaded}
          :reaction (fn [this txt file]
                      (log "loaded settings")
                      (object/raise this :settings.parse txt file))
          )

(behavior ::parse-settings
          :triggers #{:settings.parse}
          :reaction (fn [this txt file]
                      (let [forms (safe-read txt file)]
                      (log "parsed forms")
                        (when forms
                          (object/raise this :settings.parsed)  
                          ))))

(object/object* ::settings
               :tags #{:settings :loader})

(def settings (object/create ::settings))
