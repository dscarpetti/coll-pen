(ns ^:figwheel-hooks coll-pen.core
  (:require
   [coll-pen.coll :as coll]
   [coll-pen.css :as css]
   [coll-pen.delim :as delim]
   [coll-pen.demo :as demo]
   [coll-pen.search :as search]
   [reagent.core :as r]))

(defonce reload-states (atom {}))

(defn clear-dynamic-reload-states! [] (reset! reload-states {}))

(defn -draw [coll load-data-fn estimated-count-fn expanded-paths
             el-per-page disable-loading-animation always-highlight
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
                ;;:use-loading-callbacks (not auto-loading)
                :disable-loading-animation disable-loading-animation
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
         el-per-page disable-loading-animation always-highlight
         palette dynamic-reload-key edit-handler search-handler search-instructions]
      (let [highlight @tab-highlight]
        [:span {:on-key-down on-key-down
                :on-click on-click}
         css-link
         [:div.coll-pen
          {:class (if highlight "coll-pen-highlight-on" "coll-pen-highlight-off")}
          (coll/draw-el config coll [])]]))))

(defn draw [coll & {:keys [load-data-fn estimated-count-fn expanded-paths el-per-page disable-loading-animation always-highlight
                           palette dynamic-reload-key edit-handler search-handler search-instructions]
                    :or {expanded-paths '([]) el-per-page 10 palette :dark search-handler :subs}}]
  (let [el-per-page (if (and (number? el-per-page) (pos? el-per-page))
                      el-per-page
                      10)
        search-handler (search/get-search-handler search-handler)
        search-instructions (or search-instructions (:coll-pen/instructions (meta search-handler)) "Search")]
    [-draw
     coll load-data-fn estimated-count-fn expanded-paths
     el-per-page disable-loading-animation always-highlight
     palette dynamic-reload-key edit-handler search-handler search-instructions]))
;(clear-dynamic-reload-states!)
(demo/render draw)
