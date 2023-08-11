(ns ^:figwheel-hooks coll-pen.demo.main
  (:require
   [coll-pen.core :as cp :refer [draw create-css-link]]
   [coll-pen.demo.data :as data]
   [goog.dom :as gdom]
   [reagent.core :as r]
   [reagent.dom :as rdom]))


(defonce app-state (r/atom {:tab :demo
                            :demo-data data/demo
                        }))

(defonce custom-css (cp/create-css-link
                     "a.custom { color: #7cb7ba; }"
                     "a.custom:hover::after, .coll-pen-value-leaf a:focus::after { content: \" ↗\"; }"
                     "img.custom {width: 120px; transition: 0.5s;}"
                     "img.custom:hover {width: 300px;}"))

(defn demo []
  (let [state @app-state
        tab (:tab state)]
    [:div
     custom-css
     [:div.tabs
      [:button {:disabled (= tab :demo)
                :on-click #(swap! app-state assoc :tab :demo)}
       "Editable Demo"]
      [:button {:disabled (= tab :api)
                :on-click #(swap! app-state assoc :tab :api)}
       "API Explorer"]
      [:button {:disabled (= tab :examples)
                :on-click #(swap! app-state assoc :tab :examples)}
       "Examples"]]


     [:div.content
      (case tab
        :demo (cp/draw (:demo-data state)
                       {:key :example
                        :estimated-count-fn count
                        :custom-renderer (fn [val]
                                           (when (implements? data/CustomRender val)
                                             (data/render-el val)))
                        :load-data-fn (fn [coll path cb]
                                        (let [simulated-loading-time (+ 500 (rand-int 1000))]
                                          (if (and (== 2 (count path)) (= (path 0) :people))
                                            (js/setTimeout #(cb (cp/unroll-paths coll)) simulated-loading-time)
                                            (js/setTimeout cb simulated-loading-time))))
                        :edit-handler
                        (fn [{:keys [old-coll new-coll path k old-value new-value delete]} ok-cb fail-cb]
                          (condp < (rand)
                            .95 (js/setTimeout #(fail-cb "Sim Random Failure") 1000)
                            .9 (throw (ex-info "Sim Random Error" {:nothing :really}))
                            (js/setTimeout #(do
                                              (if (empty? path)
                                                (swap! app-state assoc :demo-data new-coll)
                                                (swap! app-state assoc-in (into [:demo-data] path) new-coll))
                                              (ok-cb (when (< (rand) 0.5) "Did it!"))) 1000)))})
        :api (cp/draw data/api {:truncate false
                                :key :api
                                :expanded-paths [[]
                                                 [:functions]
                                                 [:functions 'draw]
                                                 [:functions 'unroll-paths]
                                                 [:functions 'create-css-link]
                                                 [:functions 'clear-state-data!]]})

        :examples [:span
                   [:div
                    (cp/draw (:description data/examples) {:expanded-paths :all
                                                           :el-per-page 7
                                                           :edit-handler (fn [_ ok-cb fail-cb]
                                                                           (fail-cb "Editing isn't actually enabled in this example :("))})
                    (cp/draw (:map data/examples))
                    (cp/draw (:vec data/examples))
                    (cp/draw (:set data/examples))
                    (cp/draw (:seq data/examples))
                    (cp/draw (:nesting data/examples))
                    ]]



        )]]))





(defn render []
  (rdom/render [demo] (gdom/getElement "app")))

(defn ^:after-load on-reload []
  (swap! app-state update :reloads inc))

(render)
