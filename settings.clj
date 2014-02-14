{
 :+
 {:app       [:objs.app/init! ]

  :loader    [:objs.loader/load]

  :settings  [:objs.settings/load-settings
              :objs.settings/settings-loaded
              :objs.settings/parse-settings]

  :ogl       [:objs.ogl/create!
              :objs.ogl/resize!]

  :interpolator [:objs.interpolator/init!
                 :objs.interpolator/move!
                 :objs.interpolator/set!
                 :objs.interpolator/update!
                 :objs.interpolator/get
                 ]
  }
 :-
 {
  }

 }
