(ns gaz.layerproto)

(defprotocol LayerProto
  (get-scene [this])
  (get-cam [this])
  (add [this obj]))
