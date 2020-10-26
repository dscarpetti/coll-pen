(ns coll-pen.demo.data)


(def api
  {:functions
   {'draw {:description "main function for drawing a collection"
           :evaluates-to "a reagent/react component which draws the supplied collection."
           :arglists '([coll] [coll opts])
           :params {'coll {:type 'collection
                           :description "The collection to be drawn"
                           :example {:foo [:bar #{:baz}]}}

                    'opts {:type "map"
                           :description "Options for display and interactivity"
                           :example {:el-per-page 10}
                           :default {:expanded-paths '([])
                                     :estimated-count-fn 'count
                                     :el-per-page 10
                                     :truncate 35
                                     :palette :dark}
                           :options {:load-data-fn {:type 'function
                                                    :description "If provided, it will be called when a collection is first expanded with a function of 3 arguments: [coll path loaded-callback]. `coll` is the collection that is trying to load, `path` is the associative keyseq of where the collection is the provided root collection (like what would be used for `get-in`, `assoc-in`, etc.) `loaded-callback` is a function which should be called with a single argument `expanded-sub-paths`, which should be a sequence of sub-paths which were also loaded (or nil), once the data has been loaded to clear the loading animation. The provided sub-paths will be marked as loaded and automatically expanded. (You can use the `unroll-paths` helper function to generate all sub-paths of a given collection). If a load-data-fn is not supplied, it is assumed that the entire collection is already loaded."}

                                     :edit-handler {:type 'function
                                                    :description "If provided, add/remove/edit controls will appear. When and edit is made the `edit-handler` function will be called with 3 arguments: [edit-map success-callback error-callback]. The callbacks each optionally take a single argument which will display as a string to the user."
                                                    :edit-map-key {:old-coll "the collection before editing"
                                                                   :new-coll "the collection after editing"
                                                                   :path "path keyseq of the coll relative to the root"
                                                                   :key "the key in the collection which was edited"
                                                                   :old-value "the old value associated with the key"
                                                                   :new-value "the new value associated with the key :deleted -> true if the key was deleted/removed"}}

                                     :search-handler {:type #{'keyword 'function}
                                                      :default :subs
                                                      :description "One of the keywords indicating a built-in search, or a function of two arguments [coll search-string] which returns a collection of results. If nil, search controls will not be present."
                                                      :built-ins {:regex "regular expression search"
                                                                  :subs "sub-string search (default)"
                                                                  :prefix "string prefix search"
                                                                  :eq "equality/exact-match search"}}

                                     :search-instructions {:type 'string
                                                           :description "Search instructions to display to the user (can be used to override instructions for the default search handlers)"}


                                     :expanded-paths {:type #{'seq-of-paths 'keyword}
                                                      :default '([])
                                                      :example '([] [:expanded-stuff])
                                                      :description "A sequence of keyseq paths which should be expanded by default or any keyword (e.g. :all) which will cause all paths to expand by default."}


                                     :estimated-count-fn {:type 'function
                                                          :default 'count
                                                          :description "A function of one argument [coll] which supplies an estimated size of an unexpanded/unloaded collection. Defaults to `count` if no load-data-fn is supplied."}

                                     :el-per-page {:type 'integer
                                                   :default 10
                                                   :description "How many elements in a collection should be displayed before paginating"}

                                     :truncate {:type 'integer
                                                :default 35
                                                :description "The number of characters to display before truncating. Truncation is disabled if set to nil"}

                                     :palette {:type #{'keyword 'map}
                                               :default :dark
                                               :description "A keyword :dark (default) or :light which determines the fonts/color scheme. It can also be a map specifying colors (and font), missing values will be substituted from the :dark theme."
                                               :map-keys #{:background :foreground :shadow :highlight :control :active :disabled :status :error :string :keyword :symbol :number :other :idx :font}}

                                     :custom-renderer {:type 'function
                                                       :description "A function of one argument [value] which will be called to render collections or leaf values in collections. If the result is nil default rendering will be used."}

                                     :always-highlight {:type 'boolean
                                                        :description "By default, highlighting behavior is reduced when using mouse-interaction setting this to true will always use keyboard-interaction highlight behavior."}


                                     :key {:type 'any
                                           :description "Used to ensure a unique react component is created. It will also be used to preserve the internal state of the display across dynamic reloads. (Particularly useful when using tools like figwheel.) State data can be reset by calling `clear-state-data!`."}
                                     }}}}
    'unroll-paths {:description "Generates all sub-paths of a coll, useful for autoexpanding a loaded collection when calling a load-data-fn callback."
                   :evaluates-to "a sequence of keyseq paths"
                   :arglists '([coll])
                   :example "(loaded-callback (unroll-paths loaded-collection))"
                   :params {'coll {:type 'collection
                                   :description "The collection to generate paths for"}}}

    'create-css-link {:description "Creates a uri encoded data link element to inject styles"
                      :evaluates-to "A hiccup-like array for a css link element"
                      :arglists '([& css-strs])
                      :example "(create-css-link “.example {:font-weight :bold}”)"}

    'clear-state-data! {:description "Clears state associated with all :key(s)"
                        :arglists '()}}})

(defonce _records_ ;; only define these records once to ensure `instance?` & `implements?` checks work across dynamic reloads
  (do
    (defprotocol CustomRender
      (render-el [this]))

    (defrecord Hyperlink [text url]
      CustomRender
      (render-el [this]
        [:a.custom {:target "_blank" :href url} text]))

    (defrecord Image [alt src]
      CustomRender
      (render-el [this]
        [:img.custom {:alt alt :src src}]))))

(def demo
  {:people [{:name {:first "Jane" :last "Smith"}
             :age 27
             :color 'red
             :active? true}
            {:name {:first "Bill" :last "Jones"}
             :age 54
             :color 'green
             :active? true}
            {:name {:first "Alice" :middle "Eve" :last "Bob"}
             :age 60
             :color 'blue
             :active? false}
            {:name {:first "Jim"}
             :age 24
             :color 'yellow
             :active? true}]
   :nato/phonetic '(alpha bravo charlie delta echo foxtrot golf hotel
                          igloo juliet kilo lima mike november oscar
                          papa quebec romeo sierra tango uniform victor
                          whiskey xray yankee zulu)
   :some-primes #{2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199}
   :nested {:map {'namespaced/key 'namespaced/value
                  {:map :key} {:map :value}
                  ["compound" "key"] ["vector" "value"]
                  #{:set-key} #{:set-value}
                  '(:seq :key) '(:seq value)}
            :vec ['namespaced/symbol
                  ["vector" "value"]
                  {:map :value}
                  #{:set-value}
                  '(:seq value)]
            :set #{'namespaced/symbol
                   ["vector" "value"]
                   {:map :value}
                   #{:set-value}
                   '(:seq value)}
            :seq '(namespaced/symbol
                   ["vector" "value"]
                   {:map :value}
                   #{:set-value}
                   (:seq value))}
   :truncated "an unreasonably long string which will be truncated with default settings"
   :custom-renderer-examples
   {:mona-lisa
    {:url (->Hyperlink "Wikipedia - Mona Lisa" "https://commons.wikimedia.org/wiki/File:Mona_Lisa,_by_Leonardo_da_Vinci,_from_C2RMF_retouched.jpg")
     :img (->Image
           "monalisa"
           "https://upload.wikimedia.org/wikipedia/commons/e/ec/Mona_Lisa%2C_by_Leonardo_da_Vinci%2C_from_C2RMF_retouched.jpg")}}
   })


(def examples
  {:description {:coll-pen "Utility for Beautiful Data Drawings"
                 :version 0.2
                 :interactive? true
                 :possible-use-cases '(admin-console
                                       dev-visualizer
                                       app-monitor
                                       user-interface
                                       data-explorer
                                       structure-prototyper
                                       app-simulator
                                       something-else-even-cooler)
                 :features ["Beautiful Rendering"
                            "Theming"
                            "Expand/Collapse nested collections"
                            "Support for Dynamic Data Loading"
                            "Built-in Pagination"
                            "Optional Collection Searching"
                            "Optional Collection Editing"]
                 :dependencies #{'org.clojure/clojure 'org.clojure/clojurescript 'reagent}}



   :map {'a :map}
   :vec ["this" "is" "a" "vector"]
   :set #{'example/set}
   :seq '(a "sequence" :of [:elements])
   :nested {[1 2] [3 #{4 5}]}
   })
