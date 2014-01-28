(ns render.renderable)

(def renderer (atom nil))

(defn set-renderer! [new-renderer] (reset! renderer new-renderer))
(defn get-renderer  [] @renderer)

(defprotocol RenderableProto
  (render [this]))
