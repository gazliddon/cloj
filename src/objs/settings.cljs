(ns objs.settings
  (:require-macros
    [cljs.core.async.macros     :refer [go go-loop]]
    [lt.macros                  :refer [behavior]])
  
  (:require
    [cljs.reader                  :as reader]
    [cljs.core.async              :as ca :refer [chan <! >! close!]]
    [file.loader                  :as loader ]
    [cloj.jsutil                  :as jsu :refer [log strlog]]
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

(defn +behaviors [cur m]
  (reduce (fn [res [k v]]
            (update-in res [k] #(apply conj (or % '()) v)))
          cur
          m))

(defn -behaviors [cur m]
  (reduce (fn [res [k v]]
            (update-in res [k] #(remove (set v) %)))
          cur
          m))

(defn behavior-diff [{add :+ rem :- :as diff} final]
  (if-not diff
    final
    (-> final
        (+behaviors add)
        (-behaviors rem))))

(defn refresh-objs! [os]
  (doseq [obj os]
    (let [cur @obj
          behs (object/tags->behaviors (:tags cur))
          trigs (object/->triggers behs )]
      (reset! obj (assoc @obj :listeners trigs )))))

(defn new-behaviors! [forms]
  ;; TODO need to merge this in with
  ;; the settings we already have
  (->> 
    (behavior-diff forms {})
    (reset! object/tags)
    (keys) 
    (mapcat object/by-tag )
    (refresh-objs!)))

(behavior ::parse-settings
          :triggers #{:settings.parse}
          :reaction (fn [this txt file]
                      (when-let [forms (safe-read txt file) ]
                        (object/raise this :settings.parsed)
                        (new-behaviors! forms))))

(behavior ::load-settings
          :triggers #{:settings.load}
          :reaction (fn [this file]
                      (object/raise this :file.load file)))

(behavior ::settings-loaded
          :triggers #{file.loaded}
          :reaction (fn [this txt file]
                      (object/raise this :settings.parse txt file)))

(object/object* ::settings
                :tags #{:settings :loader})

(def settings (object/create ::settings))
