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

  :iterator    [:objs.iterator/set-index!
                :objs.iterator/next!
                :objs.iterator/prev!
                :objs.iterator/set-wrap!
                :objs.iterator/get-index
                :objs.iterator/get-current]

  :geo         [:objs.geo/add-to-scene!
                :objs.geo/remove-from-scene!
                :objs.geo/update!]
  }

 :-
 {
  }

 }
