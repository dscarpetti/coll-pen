# coll-pen

A ClojureScript library to interactively visualize and edit clojure collections

[![Clojars Project](https://img.shields.io/clojars/v/coll-pen.svg)](https://clojars.org/coll-pen)

## Overview

![Features Example Image](https://dscarpetti.github.io/coll-pen/images/feature-example.png)

Coll-pen renders clojure collections as interactive elements using [reagent](https://github.com/reagent-project/reagent). It was generally built for development or internal use, and is great for examining the state of an app. That said, it can certainly be used as a component in end-user applications as well.

Additionally, it is fully keyboard navigable and *designed with accessibility in mind*.

## Demo

Check out the [demo](https://dscarpetti.github.io/coll-pen)

## API

There is one main function, `coll-pen.core/draw` which takes one or two parameters
  - `coll` - the collection to be drawn
  - `opts` - optional options map
    - `:key` - Used to ensure a unique react component is created for the collection. Also used to store state across dynamic reloads (for figwheel-style development) which can be cleared with the `coll-pen.core/clear-state-data!`
    - `:load-data-fn` - A function of 3 args `[coll path loaded-callback]` which will be called when an unloaded collection is first expanded. If `nil`, all data is presumed to be loaded. `loaded-callback` takes a single optional parameter which should be a sequence of sub-paths (that have been loaded) to auto-expand.
    - `:edit-handler` - A function of 3 args `[edit-map ok-callback fail-callback]` which is called when an element is added, edited, or deleted. See [example](#edit-handler-example). *If `edit-handler` is `nil`, no edit controls will appear.* If an edit is successful `ok-callback` should be called with an optional single success-message parameter. If the update fails, `fail-callback` should be called with an optional `fail-message` parameter. The contents of the `edit-map` will be:
       - `:old-coll` - the collection before editing
       - `:new-coll` - the collection after editing
       - `:path` path - keyseq of the coll relative to the root
       - `:key` - the key in the collection which was edited
       - `:old-value` - the old value associated with the key
       - `:new-value` - the new value associated with the key :deleted -> true if the key was deleted/removed
     - `:search-handler` - either a keyword indicating one of the built-in search functions or a functions of two arguments `[coll search-string]` which should return a collection of results. If nil, search functionality will be disabled. Built-in search functions are
	  - `:subs` - substring search **(default)**
        `:regex` - regular expression search
		`:prefix` - prefix string search
		`:eq` - equality/exact-match search
	- `:search-instructions` - a string to explain searching, can be used to override the built-in search function instructions
	- `:expanded-paths` - a sequence of paths which are initially loaded/expanded. If a keyword is supplied instead, all paths will be expanded by default. **Default** `'([])`
	- `:estimated-count-fn` - a function which will be called to determine the size of a collapsed/unloaded collection. If `:load-data-fn` is nil, the default is `count`.
	- `:el-per-page` - number of elements to show in a collection before paginating. **Default** `10`
	- `:truncate` - permitted length of a string before it is truncated. Falsely value disables truncation. **Default** `35`
	- `:palette` - `:dark`, `:light`, or map with the following keys specifying colors (and font): `:background` `:foreground` `:shadow` `:highlight` `:control` `:active` `:disabled` `:status` `:error` `:string` `:keyword` `:symbol` `:number` `:other` `:idx` `:font`. Missing keys will be substituted from the `:dark` theme.
	- `:custom-renderer` - a function of one argument which will be called before rendering a collection or value. If it evaluates to nil the default renderer will be used.
	- `:always-highlight` - by default, highlighting behavior is reduced when using mouse-interaction setting this to true will always use keyboard-interaction highlight behavior.

The other available functions are:
  - `clear-state-data!` - which clears any state associated with keys (only relevant for dynamic reloading situations)
  - `unroll-paths` - a helper function useful for expanding all paths when calling a `loading-callback` function. e.g. `(loaded-callback (unroll-paths loaded-collection))`
  - `create-css-link` - a simple utility to inject stylesheet data into a page by creating a hiccup-style uri-encoded link element

## Usage Notes

The display and editing functionality varies between the four main collection types:
  - **map** - add key-value pair, edit value, delete key-value-pair
  - **vector** - add element, delete last element
  - **set** - add element, edit element, delete element
  - **sequence** - only the whole collection can be editing (by interacting with the key in the parent collection)

Nested collections are only rendered as elements if their parent is associative, otherwise they are rendered as text. So, the children of `map`s and `vector`s will be interactive collections, but the children of `set`s and `sequence`s will always be text. Map keys are always rendered as text.

## Navigation

### Mouse
  - Clicking on the opening bracket/brace will expand/collapse the collection
  - If an editor handler is present, clicking on a map key, vector index, or set element will open the editor. *To delete an element, clear the value out of editor field, if it is eligible for deletion a delete control will appear*
  - If an editor handler is present, clicking on the `+` will open the element adder
  - If pagination is visible, clicking the page indication (e.g. `3/7`) will open a page-jump field
  - If the page jump field is open and searching is enabled, clicking the `?` will open the search field.

### Keyboard
  - All interactive elements are `Tab`bable
  - `Enter` on opening bracket/brace expands/collapses an element
  - If an editor handler is present, `Enter` on a map key, vector index, or set element opens the editor
  - If pagination is focused, `ArrowLeft` and `ArrowRight` will move between pages. `Enter` will open a page-jump field and `?` will open the search field
  - If an editor is open and the value is valid `Ctrl/Cmd + Enter` will save
  - If an editor is open and is eligible for deletions `Ctrl/Cmd + Backspace/Delete` will delete
  - `Escape` will close jump, search, and editor fields

## Examples

### Setup Code

```clojure
(require '[coll-pen.core :as cp])

```

### Map

```clojure
(cp/draw {'a :map} {:key :map})
```

![Map Example Image](https://dscarpetti.github.io/coll-pen/images/map.png)


### Vector

```clojure
(cp/draw ["this" "is" "a" "vector"] {:key :vector})
```

![Vector Example Image](https://dscarpetti.github.io/coll-pen/images/vector.png)


### Set

```clojure
(cp/draw #{'example/set} {:key :set})
```

![Set Example Image](https://dscarpetti.github.io/coll-pen/images/set.png)


### Sequence

```clojure
(cp/draw '(a "sequence" :of [:elements]) {:key :seq})
```

![Sequence Example Image](https://dscarpetti.github.io/coll-pen/images/seq.png)

Note how the last item `[:element]` is not rendered as a collection because a sequence is not an associative structure.

### Nested Collections

```clojure
(cp/draw {[1 2] [3 #{4 5}]} {:key :nested})
```

![Nested Example Image](https://dscarpetti.github.io/coll-pen/images/nest.png)

Note how the map key `[1 2]`, despite being a vector, is not rendered as an interactive collection.

### Edit Handler Example

A very simple local edit handler.

```clojure
(def state (reagent/atom {:stuff :here}))

(cp/draw state {:edit-handler (fn [{:keys [path new-call]} ok-cb fail-cb]
                                (swap! state assoc-in path new-coll))})
```

### Demo

**for a more complete code example, view the `coll-pen/demo/main.cljs` source file**


## Contributions

Contributions and feature requests are welcome.

Feedback on accessibility would also be appreciated, as I am trying to improve my understanding of accessibility to better support all users in my projects.

## License

Copyright © 2020 David Scarpetti

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
