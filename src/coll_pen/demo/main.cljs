(ns ^:figwheel-hooks coll-pen.demo.main
  (:require
   [coll-pen.core :as cp :refer [draw create-css-link]]
   [coll-pen.demo.data :as data]
   [goog.dom :as gdom]
   [reagent.core :as r]
   [reagent.dom :as rdom]))


(defonce app-state (r/atom {:tab :example
                            :example-data data/examples
                            :api-data data/api}))

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
      [:button {:disabled (= tab :example)
                :on-click #(swap! app-state assoc :tab :example)}
       "General Examples"]
      [:button {:disabled (= tab :api)
                :on-click #(swap! app-state assoc :tab :api)}
       "API Explorer"]]

     [:div.content
      (if (= tab :example)
        (cp/draw (:example-data state)
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
                      .95 (js/setTimeout #(fail-cb "Random failure") 1000)
                      .90 (throw (ex-info "Random error" {:nothing :really}))
                      (js/setTimeout #(do
                                        (if (empty? path)
                                          (swap! app-state assoc :example-data new-coll)
                                          (swap! app-state assoc-in (into [:example-data] path) new-coll))
                                        (ok-cb "Did it!")) 1000)))})
        (cp/draw (:api-data state) {:truncate false
                                    :key :api
                                    :expanded-paths [[]
                                                     [:functions]
                                                     [:functions 'draw]
                                                     [:functions 'unroll-paths]
                                                     [:functions 'create-css-link]
                                                     [:functions 'clear-state-data!]]}))]]))




(defn render []
  (rdom/render [demo] (gdom/getElement "app")))

(defn ^:after-load on-reload []
  (swap! app-state update :reloads inc))

(render)
