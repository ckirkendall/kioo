# kioo

Kioo brings Enlive/Enfocus style templates to React.  This allows for much better separation between the view and logic layers of the application.  

This project is very early on in development and the API is expected to change as development progresses.

### What does kioo mean? 
Kioo is Swahili for mirror. Facebook's React library is built around the idea that the view is a reflection of your application state.


## Artifact

All artifacts are published to [clojars](https://clojars.org/kioo). Latest version is `0.3.0`:

```
[kioo "0.3.0"]
```

## Concepts

`component` is a unit of your page such as header, footer, page element. A kioo component is logically the same as a Facebook's React.js component.  What makes kioo components diffrent from React's is they take raw html from the class path and compile it into React.js nodes.  This allows you to define the structure of your page as standard html.  React.js provides something similar with JSX but it still mixes the content and the logic.  Kioo takes a diffrent aproach by allowing you to bring in static content and transform it with selectors in a manner similar to Enlive/Enfocus templates.

The biggest diffrence you will see between Enlive and Kioo is that Kioo only supports unordered transforms.  This means that you pass a map of transforms to the component and you can not guarantee what order they will be processed in.  This is due to, selection being done at compile time and transforms being done at runtime.   Selections cannot take the structure of the content at runtime into consideration.

## Quickstart tutorial

### Working With Om

Let's take a look at and example.  Here we work with David Nolen's 
[om](https://github.com/swannodette/om).  
 
```html
<!DOCTYPE html>
<html lang="en">
  <body>
    <header>
      <h1>Header placeholder</h1>
      <ul id="navigation">
        <li class="nav-item"><a href="#">Placeholder</a></li>
      </ul>
    </header>
    <div class="content">place holder</div>
  </body>
</html>
```

```clj
(ns kioo-example.core
  (:require [kioo.om :refer [content set-attr do-> substitute listen]]
            [kioo.core :refer [handle-wrapper]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [kioo.om :refer [defsnippet deftemplate]]))


(defsnippet my-nav-item "main.html" [:.nav-item]
  [[caption func]]
  {[:a] (do-> (content caption)
              (listen :onClick #(func caption)))})

(defsnippet my-header "main.html" [:header]
  [{:keys [heading navigation]}]
  {[:h1] (content heading)
   [:ul] (content (map my-nav-item navigation))})


(deftemplate my-page "main.html"
  [data]
  {[:header] (substitute (my-header data))
   [:.content] (content (:content data))})

(defn init [data] (om/component (my-page data)))

(def app-state (atom {:heading "main"
                      :content    "Hello World"
                      :navigation [["home" #(js/alert %)]
                                   ["next" #(js/alert %)]]}))

(om/root init app-state {:target  (.-body js/document)})
```

To view the example:
```bash
$ git clone https://github.com/ckirkendall/kioo.git
$ cd kioo/example/om
$ lein cljsbuild once
```
Once the javascript compiles you can open index.html in a browser.

For a more fleshed-out example, please see the Kioo implementation of
[TodoMVC](http://todomvc.com)
[exists here](http://github.com/ckirkendall/todomvc/blob/gh-pages/labs/architecture-examples/kioo/src/todomvc/app.cljs).

### Working With Reagent

Here we work with Dan Holmsand's
[Reagent](https://github.com/holmsand/reagent).  
 
```html
<!DOCTYPE html>
<html lang="en">
  <body>
    <header>
      <h1>Header placeholder</h1>
      <ul id="navigation">
        <li class="nav-item"><a href="#">Placeholder</a></li>
      </ul>
    </header>
    <div class="content">place holder</div>
  </body>
</html>
```

```clj
(ns kioo-example.core
  (:require [kioo.reagent :refer [content set-attr do-> substitute listen]]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros [kioo.reagent :refer [defsnippet deftemplate]]))

(declare data nav)


(defsnippet my-nav-item "main.html" [:.nav-item]
  [[caption func]]
  {[:a] (do-> (content caption)
              (listen :on-click func))})


(defsnippet my-header "main.html" [:header] []
  {[:h1] (content (:header @data))
   [:ul] (content (map my-nav-item (:navigation @nav)))})


(deftemplate my-page "main.html" []
  {[:header] (substitute [my-header])
      [:.content] (content (:content @data))})


(def data (atom {:header "main"
                 :content "Hello World"}))

(def nav (atom {:navigation [["home" #(swap! data
                                             assoc :content "home")]
                             ["next" #(swap! data
                                             assoc :content "next")]]}))

(reagent/render-component [my-page] (.-body js/document))

```

To view the example:
```bash
$ git clone https://github.com/ckirkendall/kioo.git
$ cd kioo/example/reagent
$ lein cljsbuild once
```
Once the javascript compiles you can open index.html in a browser.


### Selector Syntax

Kioo uses enlive based selectors. See [syntax.html](http://enlive.cgrand.net/syntax.html)

Some examples:

```
Enlive                                       CSS
=======================================================
[:div]                                       div
[:body :script]                              body script
#{[:ul.outline :> :li] [:ol.outline :> li]}  ul.outline > li, ol.outline > li
[#{:ul.outline :ol.outline} :> :li]          ul.outline > li, ol.outline > li
[[#{:ul :ol} :.outline] :> :li]              ul.outline > li, ol.outline > li
[:div :> :*]                                 div > *
[:div :> text-node]                          (text children of a div)
[:div :> any-node]                           (all children (including text nodes and comments) of a div)
{[:dt] [:dl]}                                (fragments starting by DT and ending at the *next* DD)
```

## Transformations

A transformation is a function that returns either a react node or collection of react nodes.

Kioo transforms mirror most of the base enlive transformations:

_New Transforms_

```clojure
;; attached event listeners to the component
(listen :on-click (fn [...] ...))

;;supported react events
:on-mount
:on-render
:on-update

;;all standard dom events are supported

```

_Enlive Based Transforms_

```clojure
;; Replaces the content of the element. Values can be nodes or collection of nodes.
(content "xyz" a-node "abc")


;; Replaces the content of the element with the html provided.
(html-content "<h1>this is html text</h1>")

;; Wraps selected node into the given tag
(wrap :div)
;; or
(wrap :div {:class "foo"})

;; Opposite to wrap, returns the content of the selected node
unwrap

;; Sets given key value pairs as attributes for selected node
(set-attr :attr1 "val1" :attr2 "val2")

;; Removes attribute(s) from selected node
(remove-attr :attr1 :attr2)

;; Sets class attr of the selected node
(set-class "foo bar")

;; Adds class(es) on the selected node
(add-class "foo" "bar")

;; Removes class(es) from the selected node
(remove-class "foo" "bar")

;; Set styles on to the selected node
(set-style :display "none" :backgroud-color "#cfcfcf")

;; Removes styles from the selected node
(remove-style :display :background-color)

;; Chains (composes) several transformations. Applies functions from left to right.
(do-> transformation1 transformation2)

;; Appends the values to the content of the selected element.
(append "xyz" a-node "abc")

;; Prepends the values to the content of the selected element.
(prepend "xyz" a-node "abc")

;; Inserts the values after the current selection (node or fragment).
(after "xyz" a-node "abc")

;; Inserts the values before the current selection (node or fragment).
(before "xyz" a-node "abc")

;; Replaces the current selection (node or fragment).
(substitute "xyz" a-node "abc")


```
Not supported yet

```clojure

;; Clones the selected node, applying transformations to it.
(clone-for [item items] transformation)
(;; or
(clone-for [item items]
  selector1 transformation1
  selector2 transformation2)

;;;; Takes all nodes (under the current element) matched by src-selector, removes
;; them and combines them with the elements matched by dest-selector.
(move)
```



## Thanks

This library is based on Christophe Grand's [enlive](https://github.com/cgrand/enlive) library.

## License

Copyright © 2014 Creighton Kirkendall

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
