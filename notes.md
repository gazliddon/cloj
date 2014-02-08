# Notes on lt behaviours system

## Behaviours
Behaviours are identified by a name

It has a set of triggers and reaction function when 

Here is an example:

```
(behavior ::on-changed
                  :triggers #{:change :create}
                  :reaction (fn [this]
                              (parser this (editor/->val this))))
```

## Object (template)
An object template has a name and custom attributes. It can also have an initialisation function called :init. This is used when you create an object.
```
(object/object* ::app
                :tags #{:app :window}
                :delays 0
                :init (fn [this]
                        (ctx/in! :app this)))
```

## Creating objects
You create an object by calling:

```
(object/create ::template-id args)
```

This will call the template initialisation function with args

## Perform a behaviour

You get an object to perform a behaviour by calling

```
(object/raise* obj :behaviour-id args)
```

## Tags
These seem irrelevant. I think they're there for debugging purposes. I can't find any assigned tag used in the code.

Ah no. Tags tie behaviours to objects. Tags are hierarchical split by periods.


