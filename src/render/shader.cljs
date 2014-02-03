(ns render.shader
  (:require
    [ui.editable     :as  editable]
    [ui.attrs        :as attrs]
    [cloj.jsutil     :as jsu]
    ))


(defn remove-types [unis types-2-go])

(def remove-for-vertex [:tex2d])

(defn seq-contains? [coll target] (some #(= target %) coll))

(defn mk-vertex-unis [unis]
  (filter
    (fn[[k v]] (not  (seq-contains? remove-for-vertex (:type v) )))
    unis))

(defn- mk-shader-text [txt unis]
  (str (attrs/attrs-to-uniform-str unis) "\n" txt))

(defn- mk-shader-material-map [edn]
  (let [all-unis   (merge (:uniforms edn) (:editable edn))
        uni-prop   (attrs/mk-material-init-from-uniforms all-unis) ]
    (js-obj
      "fragmentShader"  (mk-shader-text (:frag edn) all-unis)
      "vertexShader"    (mk-shader-text (:vert edn) (mk-vertex-unis all-unis)) 
      "uniforms"        uni-prop)))

(defrecord Shader [material edn]
  editable/UIEditable

  (add-to-dat [_ dat]
    (doseq [[k v] (:editable edn)]
      )))

(defn mk-material [edn]
  (let [mat-map (mk-shader-material-map edn)
        mat (js/THREE.ShaderMaterial. mat-map)]
    (Shader. mat edn)))

