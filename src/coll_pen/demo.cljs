(ns ^:figwheel-hooks coll-pen.demo
  (:require
   [goog.dom :as gdom]
   [reagent.core :as r]
   [reagent.dom :as rdom]))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (r/atom {:reloads 0 :z 1 :x (range 100) :y {:name {:first "foo" :last "bar"}}
                            'x 1
                            'some/thing 'lalala/la
                            :some/thing :lalala/la
                            :eleven (range 11)
                            :text-map {:alpha 10
                                       :bravo "hi mom"
                                       :charlie "charlie"
                                       :delta "delta"
                                       :echo "epsilon"
                                       :foxtrot true
                                       :golf true
                                       :hotel true
                                       :igloo true
                                       :juliet true
                                       :kilo true
                                       :lima true
                                       :mike false
                                       :november false
                                       :oscar true
                                       :papa false
                                       :quebec false
                                       :romeo true
                                       :sierra false
                                       :tango true
                                       :uniform false
                                       :victor false
                                       :whiskey true
                                       :xray true
                                       :yankee false
                                       :zulu true}
                            :text-vec [:alpha "bravo" :charlie "delta" :echo "foxtrot" :golf "hotel" :igloo "juliet" :kilo "lima"
                                       :mike "november" :oscar "papa" :quebec "romeo" :sierra "tango" :uniform "victor"
                                       :whiskey "xray" :yankee "zulu"]
                            :text-set #{:alpha "bravo" :charlie "delta" :echo "foxtrot" :golf "hotel" :igloo "juliet" :kilo "lima"
                                        :mike "november" :oscar "papa" :quebec "romeo" :sierra "tango" :uniform "victor"
                                        :whiskey "xray" :yankee "zulu"}
                            :text-seq '(:alpha "bravo" :charlie "delta" :echo "foxtrot" :golf "hotel" :igloo "juliet" :kilo "lima"
                                               :mike "november" :oscar "papa" :quebec "romeo" :sierra "tango" :uniform "victor"
                                               :whiskey "xray" :yankee "zulu")}))

(defn demo [draw]
  (let [state @app-state]
    [:div
     (draw state :dynamic-reload-key ::demo
           :estimated-count-fn count
           :load-data-fn (fn [coll path cb]
                           (js/setTimeout cb 1000))
           ;;:palette :light
           ;:search-handler false
           :edit-handler
           (fn [{:keys [old-coll new-coll path k old-value new-value delete]} ok-cb fail-cb]
             (condp < (rand)
               10.9 (js/setTimeout #(fail-cb "Random failure") 1000)
               10.8 (throw (ex-info "some error" {:nothing :really}))
               (js/setTimeout #(do (swap! app-state assoc-in path new-coll) (ok-cb)) 1000))))]))
           ;; (if (= v :coll-pen/delete)
           ;;     (if (set? new-coll)
           ;;       (if (empty? path)
           ;;         (swap! app-state disj k)
           ;;         (swap! app-state update-in path disj k))
           ;;       (swap! app-state update-in path dissoc k))

           ;;     (if (set? new-coll)
           ;;       (if (empty? path)
           ;;         (swap! app-state #(-> % (disj k) (conj v)))
           ;;         (swap! app-state update-in path #(-> % (disj k) (conj v))))
           ;;       (swap! app-state assoc-in (conj path k) v)))))]))


(defn render [draw]
  (rdom/render [demo draw] (gdom/getElement "app")))

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (swap! app-state update :reloads inc)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
