(ns test.beh
  (:require-macros
    [cljs.core.async.macros    :refer [go go-loop]]
    [lt.macros                 :refer [behavior]])
  
  (:require
    [cljs.core.async            :as ca :refer [chan <! >! close!]]
    [file.loader                :as load ]
    [cloj.jsutil                :as jsu :refer [log]]
    [lt.object                  :as object]))




