(ns file.loader
  (:require
    [goog.net.XhrIo            :as xhr]
    [cljs.core.async           :as ca :refer [chan <! >! put! close!]]
    [cljs.reader               :as reader]
    [cloj.jsutil :as jsu :refer [log]]
    )
  (:require-macros
    [cljs.core.async.macros   :refer [go go-loop]]
    )
  )

(defn GET [url]
  (let [ch (chan 1)]
    (xhr/send url
              (fn [event]
                (let [res (-> event .-target .getResponseText)]
                  (go (>! ch res)
                      (close! ch)))))
    ch))

(defn- safe-read [s file]
  (when s
    (try
      (reader/read-string s)
      (catch js/global.Error e
        (log (str "Invalid settings file: " file "\n" e))
        nil)
      )))

(defn load [file]
  (GET file))

(defn read [file]
  (let [ret-ch (chan 1)
        load-ch (load file)]
    (go
      (let [ ftext (<! load-ch) ]
        (>! ret-ch (safe-read ftext file) )
        (close! ret-ch)
        ))

    ret-ch))

