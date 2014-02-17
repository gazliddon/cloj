{
 :+
 {:app          [:objs.app/init! ]

  :loader       [:objs.loader/load]

  :settings     [:objs.settings/load-settings
                 :objs.settings/settings-loaded
                 :objs.settings/parse-settings]

  :ogl          [:objs.ogl/create!
                 :objs.ogl/resize!]

  :interpolator [:objs.interpolator/init!
                 :objs.interpolator/move!
                 :objs.interpolator/set!
                 :objs.interpolator/update!
                 :objs.interpolator/get
                 ]

  :mainapp     [:cloj.core/update!
                :cloj.core/render]
  
  :feedback    [:objs.feedback/flip!
                :objs.feedback/set-rt!
                :objs.feedback/get-rt
                :objs.feedback/render-with-rt
                :objs.feedback/set-effect!]

  :iterator    [:objs.iterator/set-index!
                :objs.iterator/next!
                :objs.iterator/prev!
                :objs.iterator/set-wrap!
                :objs.iterator/get-index
                :objs.iterator/get-current]

  :geo         [:objs.geo/add-to-scene!
                :objs.geo/remove-from-scene!
                :objs.geo/update!]

  :layer       [:objs.layer/add!
                :objs.layer/set-clear-color!
                :objs.layer/render
                :objs.layer/get-rt ]

  }

 :-
 {
  }

 }
