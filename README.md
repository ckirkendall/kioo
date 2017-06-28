# kioo

Kioo brings Enlive/Enfocus style templates to React.  This allows for much better separation between the view and logic layers of the application.  

This project is very early on in development and the API is expected to change as development progresses.

### What does kioo mean? 
Kioo is Swahili for mirror. Facebook's React library is built around the idea that the view is a reflection of your application state.


## Artifact

All artifacts are published to [clojars](https://clojars.org/kioo). Latest stable version is `0.4.2`:

```
[kioo "0.5.0"] ;stable

[kioo "0.5.1-SNAPSHOT"] ;experimental
```

## Concepts

`component` is a unit of your page such as header, footer, page element. A kioo component is logically the same as a Facebook's React.js component.  What makes kioo components different from React's is they take raw html from the class path and compile it into React.js nodes.  This allows you to define the structure of your page as standard html.  React.js provides something similar with JSX but it still mixes the content and the logic.  Kioo takes a different aproach by allowing you to bring in static content and transform it with selectors in a manner similar to Enlive/Enfocus templates.

The biggest difference you will see between Enlive and Kioo is that Kioo only supports unordered transforms.  This means that you pass a map of transforms to the component and you can not guarantee what order they will be processed in.  This is due to, selection being done at compile time and transforms being done at runtime.   Selections cannot take the structure of the content at runtime into consideration.


### Templates and Snippets

A snippet is a function that returns a kioo component, it can be used as
a building block for more complex templates, snippets and components.

You define a snippet by providing a remote resource, a selector and
series of transformations.

The snippet definition below selects a table body from the remote
resource `templates/template1.html` and grabs the first row.  It then
fills the content of the row.

```clj
(defsnippet snippet2 "templates/template1.html" [:tbody :> first-child]
  [fruit quantity]
  {[:tr :> first-child] (content fruit)
   [:tr :> last-child] (content (str quantity))})
```

A template is very similar to a snippet except it does not require a
selector to grap a sub section, instead the entire remote resource is
used as the dom.  If the remote resource is a full html document only
what is inside the body tag is brought into the template.

```clj
(deftemplate template2 "/templates/template1.html" 
  [fruit-data]
  {[:#heading1] (content "fruit")
   [:thead :tr :> last-child] (content "quantity")
   [:tbody] (content
              (map #(snippit2 % (fruit-data %)) (keys fruit-data)))})
```

### Troubleshooting

The best way to troubleshoot the processing of Kioo templates and snippets and the matching of selectors is to use Clojure's [`macroexpand-1`](http://clojuredocs.org/clojure_core/clojure.core/macroexpand-1) together with  [clojure.tools.trace](https://github.com/clojure/tools.trace)/[trace-ns](http://clojure.github.io/tools.trace/#clojure.tools.trace/trace-ns) applied to `'kioo.core` to see the resulting ClojureScript and log of what happened during the processing. You can also call manually the resulting JavaScript function `<your ns>.<snippet/template name>` and examine the React component it produces.

Read more and additional tips in the post [Kioo: How to Troubleshoot Template Processing](http://theholyjava.wordpress.com/2014/04/08/kioo-how-to-troubleshoot-template-processing/) by Jakub Holy.

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

Kioo uses enlive based selectors. See [syntax.html](http://cgrand.github.io/enlive/syntax.html)

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

Notice that some of the predefined selector steps are used as naked symbols, i.e. 
you can just use them (no require :refer etc.). Example:

```clojure
(defsnippet shop-html "templates/shops.html" [[:.shop first-of-type]]
  [_] ;;                                                 ^- a naked symbol
  {[:.content] (content "Kioo is mighty!")})
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
