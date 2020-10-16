(ns ^:figwheel-hooks coll-pen.demo
  (:require
   [coll-pen.core :refer [draw]]
   [goog.dom :as gdom]
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(defrecord Hyperlink [text url])
(defrecord Image [alt src])
;; define your app data so that it doesn't get over-written on reload
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

(def custom-css [:link {:rel "stylesheet" :type "text/css"
                        :href
                        (coll-pen.css/to-encoded-css-uri
                         (str
                          ".coll-pen-value a:hover::after, .coll-pen-value-leaf a:focus::after { content: \" ↗\"; }
                           .coll-pen-value a { color: #7cb7ba; }
                           .coll-pen-value img {width: 100px}"))}])




(defn demo []
  (let [state @app-state]
    [:div
     custom-css
     (draw state :dynamic-reload-key ::demo
           :estimated-count-fn count
           :custom-renderer (fn [val]
                              (cond
                                (instance? Hyperlink val) [:a {:target "_blank" :href (:url val)} (:text val)]
                                (instance? Image val) [:img {:alt (:alt val) :src (:src val)}]))
           :load-data-fn (fn [coll path cb]
                           (js/setTimeout cb 1000))
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
