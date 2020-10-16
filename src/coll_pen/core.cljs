(ns coll-pen.core
  (:require
   [coll-pen.coll :as coll]
   [coll-pen.css :as css]
   [coll-pen.delim :as delim]
   [coll-pen.search :as search]
   [reagent.core :as r]))

(defonce reload-states (atom {}))

(defn clear-dynamic-reload-states!
  "clears state associated with any :dynamic-reload-key(s)"
  []
  (reset! reload-states {}))

(defn -draw [coll load-data-fn estimated-count-fn expanded-paths
             el-per-page always-highlight custom-renderer truncate
             palette dynamic-reload-key edit-handler search-handler search-instructions]
  (let [css-link (css/gen-css-link-el palette)
        depth-colors (css/get-depth-colors palette)
        depth-color-count (count depth-colors)
        supplied-expanded-paths expanded-paths
        expanded-paths (when (coll? expanded-paths) expanded-paths)
        init-states (if (or (not dynamic-reload-key) (nil? (@reload-states dynamic-reload-key)))
                      (atom (reduce (fn [m path] (assoc m path {:expanded true :loaded true})) {} expanded-paths))
                      (@reload-states dynamic-reload-key))
        config {:depth-colors depth-colors
                :edit-handler edit-handler
                :search-handler search-handler
                :search-instructions search-instructions
                :init-states init-states
                :default-expanded (keyword? supplied-expanded-paths)
                :custom-renderer custom-renderer
                ;;:use-loading-callbacks (not auto-loading)
                ;;:disable-loading-animation disable-loading-animation
                :truncate truncate
                :load-data-fn load-data-fn
                :el-per-page el-per-page
                :get-delim-color (fn [path] (depth-colors (rem (count path) depth-color-count)))
                :get-collapsed-content (if estimated-count-fn
                                         (fn [coll]
                                           (let [v (str (estimated-count-fn coll))]
                                             (if (empty? v)
                                               "…"
                                               v)))
                                         (constantly "…"))}
        tab-highlight (r/atom true)
        on-key-down (when-not always-highlight
                      (fn [e]
                        (when (and (not @tab-highlight) (= (.-key e) "Tab"))
                          (reset! tab-highlight true))))
        on-click (when-not always-highlight
                   (fn [e]
                     (when @tab-highlight
                       (reset! tab-highlight false))))]

    (when (and dynamic-reload-key (nil? (@reload-states dynamic-reload-key)))
      (swap! reload-states assoc dynamic-reload-key init-states))

    (fn [coll load-data-fn estimated-count-fn expanded-paths
         el-per-page always-highlight custom-renderer truncate
         palette dynamic-reload-key edit-handler search-handler search-instructions]
      (let [highlight @tab-highlight]
        [:span {:on-key-down on-key-down
                :on-click on-click}
         css-link
         [:div.coll-pen
          {:class (if highlight "coll-pen-highlight-on" "coll-pen-highlight-off")}
          (coll/draw-el config coll [])]]))))

(defn draw
  "Evaluates to a reagent/react component which draws the supplied collection.

  keyword options:
    :load-data-fn -> if provided, it will be called when a collection is first expanded
                     with a function of 3 arguments: [coll path loaded-callback]. `coll`
                     is the collection that is trying to load, `path` is the associative
                     keyseq of where the collection is the provided root collection (like
                     what would be used for `get-in`, `assoc-in`, etc. `loaded-callback` is
                     a function which should be called with zero arguments once the data
                     has been loaded to clear the loading animation. If a load-data-fn is
                     not supplied, it is assumed that the entire collection is already
                     loaded.

    :edit-handler -> if provided, add/remove/edit controls will appear. When and edit is made
                     the `edit-handler` function will be called with 3 arguments:
                     [edit-map success-callback error-callback]. The callbacks each optionally
                     take a single argument which will display as a string to the user. The
                     edit-map is of the form: {:old-coll -> the collection before editing
                                               :new-coll -> the collection after editing
                                               :key -> the key in the collection which was edited
                                               :old-value -> the old value associated with the key
                                               :new-value -> the new value associated with the key
                                               :deleted -> true if the key was deleted/removed}

    :search-handler -> one of the keywords indicating a built-in search function:
                        :regex -> regular expression search
                        :subs (default) -> sub-string search
                        :prefix -> string prefix search
                        :eq -> equality/exact match search

                       optionally a function can be supplied which takes two arguments
                       [coll search-string] and returns a collection of results.

                       if set to nil, search controls will not be present

    :search-instructions -> Search instructions to display to the user (can be used to
                            override instructions for the default search handlers)


    :expanded-paths -> a sequence of keyseq paths which should be expanded by default OR
                       any keyword (e.g. :all) which will cause all paths to expand by
                       default.

    :estimated-count-fn -> a function of one argument [coll] which supplies an estimated
                           size of an unexpanded/unloaded collection. Defaults to `count`
                           if no load-data-fn is supplied.

    :el-per-page -> how many elements in a collection should be displayed before paginating

    :truncate -> number of characters before truncation (no truncation if nil) defaults to 35

    :palette -> a keyword :dark (default) or :light which determines the fonts/color scheme. It
                can also be a map containing the following keys specifying colors (and font)
                   :background   :foreground   :shadow    :highlight
                   :control      :active       :disabled  :status    :error
                   :string       :keyword      :symbol    :number    :other  :idx
                missing keys will be substituted from the :dark theme. If a font is specified,
                it should be monospaced.

    :custom-renderer -> a function of one argument [value] which will be called to render
                        collections or leaf values in collections.
                        If the result is nil default rendering will be used.

    :always-highlight -> by default highlighting behavior is reduced when using mouse-interaction
                         setting this to true will always use keyboard-interaction highlight
                         behavior.

    :dynamic-reload-key -> if provided, it will be used to preserve the internal state of the
                           display across dynamic reloads. Useful when using tools like figwheel."
  [coll & {:keys [load-data-fn edit-handler search-handler search-instructions
                  expanded-paths estimated-count-fn el-per-page truncate palette custom-renderer
                  always-highlight dynamic-reload-key ]
                    :or {expanded-paths '([]) el-per-page 10 truncate 35 palette :dark search-handler :subs}}]
  (let [el-per-page (if (and (number? el-per-page) (pos? el-per-page))
                      el-per-page
                      10)
        estimated-count-fn (if estimated-count-fn estimated-count-fn (when-not load-data-fn count))
        search-handler (search/get-search-handler search-handler)
        truncate (when (int? truncate) truncate)
        search-instructions (or search-instructions (:coll-pen/instructions (meta search-handler)) "Search")]
    [-draw
     coll load-data-fn estimated-count-fn expanded-paths
     el-per-page always-highlight custom-renderer truncate
     palette dynamic-reload-key edit-handler search-handler search-instructions]))
