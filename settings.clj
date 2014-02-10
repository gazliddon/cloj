;; Default behaviours
{
 :+
 {:app        [objs.app/init! ]

  :settings  [objs.settings/load-settings
              objs.settings/parse-settings]

  :ogl-window [objs.ogl/create!]
  }
 :-
 {}
 
 }
