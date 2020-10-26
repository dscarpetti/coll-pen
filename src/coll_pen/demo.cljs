(ns ^:figwheel-hooks coll-pen.demo
  (:require
   [coll-pen.core :as cp :refer [draw create-css-link]]
   [goog.dom :as gdom]
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(defonce _records_
  (do
    (defrecord Hyperlink [text url])
    (defrecord Image [alt src])
    nil))

(def app-state (r/atom {;;:reloads 0
                        :people [{:name {:first "Jane" :last "Smith"}
                                  :age 27
                                  :color 'red
                                  :active? true
                                  }
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
                                       '(:seq :key) '(:seq value)
                                       }
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
                        {:mona-lisa {:url (->Hyperlink "Wikipedia - Mona Lisa" "https://commons.wikimedia.org/wiki/File:Mona_Lisa,_by_Leonardo_da_Vinci,_from_C2RMF_retouched.jpg")
                                     :img (->Image
                                           "monalisa"
                                           "https://upload.wikimedia.org/wikipedia/commons/e/ec/Mona_Lisa%2C_by_Leonardo_da_Vinci%2C_from_C2RMF_retouched.jpg")}}
                        }))

(def custom-css (cp/create-css-link
                 "a.custom { color: #7cb7ba; }"
                 "a.custom:hover::after, .coll-pen-value-leaf a:focus::after { content: \" ↗\"; }"
                 "img.custom {width: 120px; transition: 0.5s;}"
                 "img.custom:hover {width: 300px;}"))

(defn demo []
  (let [state @app-state]
    [:div
     custom-css
     (cp/draw state
              :dynamic-reload-key ::demo
              :estimated-count-fn count
              :custom-renderer (fn [val]
                                 (cond
                                   (instance? Hyperlink val) [:a.custom {:target "_blank" :href (:url val)} (:text val)]
                                   (instance? Image val) [:img.custom {:alt (:alt val) :src (:src val)}]))
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
                                      (reset! app-state new-coll)
                                      (swap! app-state assoc-in path new-coll))
                                    (ok-cb "Did it!")) 1000))))]))


(defn render []
  (rdom/render [demo] (gdom/getElement "app")))

(defn ^:after-load on-reload []
  (swap! app-state update :reloads inc))

(render)
