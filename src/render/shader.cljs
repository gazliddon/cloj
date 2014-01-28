(ns render.shader
  (:require
    [ui.editable :as  editable] 
    [clojure.string :as string]
    [cloj.jsutil :as jsu]
    ))

(def type-to-id {"f" "float"
                 "t" "sampler2d"})

(defn- gen-uniform-shader-text-entry [k v]
  (str "uniform "  (type-to-id (:type v)) " " (name k) ";"))

(defn- gen-uniform-shader-text [unis]
  (map #(gen-uniform-shader-text-entry (first %) (first (rest %))) unis))

(defn- mk-shader-text [effect typ]
  (let [uni (:uniforms effect)
        uni-text (string/join "\n" (gen-uniform-shader-text uni))]
  (str uni-text "\n" (typ effect))))

(defn- mk-shader-uniforms [effect]
  (clj->js (:uniforms effect)))

(defn- mk-shader-material-map [edn]
  (let [ shader-map
        (js-obj
          "fragmentShader"  (mk-shader-text edn :frag)
          "vertexShader"    (mk-shader-text edn :vert) 
          "uniforms" (js-obj
                       "time"       (js-obj "type" "f" "value" 0.0)
                       "thisScreen" (js-obj "type" "t" "value" nil)
                       "lastScreen" (js-obj "type" "t" "value" nil)
                       ))
        uni-prop (aget shader-map "uniforms")]

    (doseq [[k v] (:uniforms edn)]
      (aset uni-prop (name k) (clj->js v)))
    shader-map))

(defrecord Shader [material edn]
  editable/UIEditable

  (add-to-dat [_ dat]
    (doseq [[k v] (:uniforms edn)]
      (let [ prop   (jsu/get-prop material "uniforms" (name k)) ]
        (-> dat
          (.add prop "value" (:min v) (:max v))
          (.name (or (:nice-name v) (name k)))
          )))))

(defn mk-material [edn]
  (let [mat-map (mk-shader-material-map edn)
        mat (js/THREE.ShaderMaterial. mat-map)]
    (Shader. mat edn)))

