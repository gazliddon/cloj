(ns gaz.effects)

(def basic-shader
  { :uniforms {:u_x_scale    {:type "f" :value 0.99 :min 0.001 :max 2 :nice-name "X"}
               :u_y_scale    {:type "f" :value 0.99 :min 0.001 :max 2 :nice-name "Y"}
               :u_lpix_scale {:type "f" :value 0.1 :min -0.2 :max 0.2}
               :u_mix        {:type "f" :value 0.1 :min -1 :max 10 :nice-name "Mix"}
               :u_fpix_scale {:type "f" :value 2.0 :min -5 :max 5} }

   :frag "
         varying vec2       vUv;
         uniform sampler2D  lastScreen;
         uniform sampler2D  thisScreen;

         uniform float      time;

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
         vec4 thisPix = texture2D (thisScreen, thisUv);
         vec4 finalPix = (thisPix+ (lastPix * u_lpix_scale));

         finalPix = mix(thisPix, frct(finalPix), u_mix);

         finalPix = u_fpix_scale * finalPix * lastPix;

         gl_FragColor = finalPix ; 
         } "
   } )

(def effects
  {:basic-shader basic-shader}
  )
