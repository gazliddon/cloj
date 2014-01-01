(ns gaz.renderable)

(defprotocol RenderableProto
  (render [this renderer]))
