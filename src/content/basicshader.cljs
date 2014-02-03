(ns content.basicshader
  (:require
    [ui.attrs :as attrs :refer [v3 fl tex2d]]
    ))

(def basic-shader
  {
   :uniforms  {:lastScreen (tex2d nil)
               :thisScreen (tex2d nil)
               :inputScreen (tex2d nil)
               :time (fl 0) }

   :editable  {:u_x_scale (fl 0.99 0.001 10)
               :u_y_scale (fl 0.99 0.001 10)
               :u_lpix_scale (fl 0.1 -2 2)
               :u_mix (fl 0.1 -5 5)
               :u_fade (fl 1.0 0 1)
               :u_fpix_scale (fl 2 -5 5) }

   :vert "
         varying vec2 vUv;

         void main() {
         vUv = uv;
         gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
         } "

   :frag "
         varying vec2       vUv;

         vec2 scaleVec2 (vec2 v, vec2 scale, vec2 offset) {
         vec2 tmp = v - offset;
         tmp = tmp * scale;
         tmp = tmp + offset;
         return tmp;
         }

         vec4 frct(vec4 v) {
         return (v - floor(v)); }

         void main() {
         vec2 thisUv = vUv;
         vec2 lastUv = vUv;
         lastUv = scaleVec2(lastUv, vec2(u_x_scale, u_y_scale), vec2(0.5, 0.5));

         vec4 lastPix = texture2D (lastScreen, lastUv);
         lastPix = lastPix * u_fade;
         vec4 thisPix = texture2D (thisScreen, thisUv);
         vec4 finalPix = (thisPix+ (lastPix * u_lpix_scale));

         finalPix = mix(thisPix, frct(finalPix), u_mix);
         finalPix = u_fpix_scale * finalPix * lastPix;

         gl_FragColor = finalPix ; 
         } "
   } )


