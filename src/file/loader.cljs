(ns file.loader
  (:require
    [goog.net.XhrIo                :as xhr]
    [cljs.core.async               :as ca :refer [chan <! >! put! close!]]
    )
  (:require-macros
    [cljs.core.async.macros   :refer [go go-loop]]
    )
  )

(defn- GET [url]
  (let [ch (chan 1)]
    (xhr/send url
              (fn [event]
                (let [res (-> event .-target .getResponseText)]
                  (go (>! ch res)
                      (close! ch)))))
    ch))

(defn load [file]
 (GET file) )



