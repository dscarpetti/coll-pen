(ns coll-pen.paginator
  (:require
   [coll-pen.input :as input]
   [reagent.core :as r]))

(defn parse-page-index [s]
  (let [n (js/parseInt s)]
    (cond
      (js/isNaN n) 0
      (<= n 0) 0
      :else (dec (int n)))))


(defn button [label title on-click disabled]
  [:a.coll-pen-button.coll-pen-noselect
   {:class (when disabled "coll-pen-disabled")
    :href (when-not disabled (str "#" title)) :title title :aria-label title :role "button"
    :on-click #(do (.preventDefault %) (.stopPropagation %) (on-click))}
   label])

(defn search-paginator [focus-key search search-page set-search! clear-search! close!]
  (let [dom-el (atom nil)
        style (r/atom nil)
        handle-change! #(do (reset! style {:min-width (.-clientWidth @dom-el)}) (set-search! %))
        handle-blur! #(reset! style nil)]
    (fn [focus-key search search-page set-search! clear-search! close!]
      [:span.coll-pen-coll-paginator
       {:style @style
        :ref (fn [el] (reset! dom-el el))}
       (button "#" "switch-to-jump" clear-search! false)
       [:span.coll-pen-spacer]
       [input/text focus-key
        search handle-change! {"Escape" close! :on-blur handle-blur!} identity 100 "?" 5 "search term"]
       [:span.coll-pen-spacer]
       (button "×" "close" close! false)])))

(defn jump-paginator [focus-key current-page change-page! close-jumper! open-search!]
  (let [dom-el (atom nil)
        style (r/atom nil)
        handle-change! #(do (reset! style {:min-width (.-clientWidth @dom-el)}) (change-page! %))
        handle-blur! #(reset! style nil)
        close-accept! #(close-jumper! false)
        close-cancel! #(close-jumper! true)]
    (fn [focus-key current-page change-page! close-jumper! open-search!]
      [:span.coll-pen-coll-paginator
       ;:on-mouse-over(fn[e](when-not @style(reset! style{:min-width(.-clientWidth @dom-el)}))):on-mouse-out(fn[e](reset! style nil))
       {:style @style
        :ref (fn [el] (reset! dom-el el))}
       (when open-search! (button "?" "switch-to-search" open-search! false))
       [:span.coll-pen-spacer]
       [input/text focus-key
        (inc (or current-page 0)) handle-change!
        {"?" open-search! "Enter" close-accept! "Escape" close-cancel! :on-blur handle-blur!}
        parse-page-index nil "#" 3 "page number" ]
       [:span.coll-pen-spacer]
       (button "✓" "jump" close-accept! false)
       (button "×" "cancel" close-cancel! false)
       ])))

(defn nav-key-handler [change-page! focus-page! open-jumper! open-search!]
  (fn [e]
    (if-not (or (.-altKey e) (.-ctrlKey e) (.-shiftKey e) (.-metaKey e))
      (when-let [loc (case (.-key e)
                       "ArrowUp" :first
                       "ArrowDown" :last
                       "ArrowLeft" :prev
                       "ArrowRight" :next
                       nil)]
        (.preventDefault e)
        (.stopPropagation e)
        (focus-page!)
        (change-page! loc))
      (when-not (or (.-altKey e) (.-ctrlKey e) (.-metaKey e))
        (case (.-key e)
          "#" (when open-jumper! (println "opening jumper") (open-jumper!))
          "?" (when open-search! (open-search!))
          nil)))))

(defn base-paginator [focus-key coll el-per-page current-page last-page change-page! open-jumper! open-search!]
  (let [dom-el (atom nil)
        page-el (atom nil)
        style (r/atom nil)
        focus-page! #(.focus @page-el)]
    (r/create-class
     {:display-name "paginator"
      :component-did-mount #(input/focus-on-mount page-el (str focus-key "-page"))
      :reagent-render
      (fn [focus-key coll el-per-page current-page last-page change-page! open-jumper! open-search!]
        (let [current-page-name (inc current-page)
              total-pages (inc last-page)
              open-jumper! #(open-jumper! current-page)]
          [:span.coll-pen-coll-paginator
           {:style @style
            :ref (fn [el] (reset! dom-el el))
            :on-key-down (nav-key-handler change-page! focus-page! open-jumper! open-search!)
            :on-mouse-over (fn [e] (when-not @style (reset! style {:width (.-clientWidth @dom-el)})))
            :on-mouse-out (fn [e] (reset! style nil))}
           (when (< 9 last-page)
             (button "«" "first-page" #(do (focus-page!) (change-page! 0)) (== 0 current-page)))
           (button "‹" (str "previous-page-" current-page) #(do (when (== current-page 1) (focus-page!))
                                                                (change-page! :prev))
                   (== 0 current-page))
           [:a.coll-pen-page.coll-pen-button.coll-pen-noselect
            {:ref #(reset! page-el %)
             :href "#jump" :title "jump/search" :role "button"
             :aria-label (str "page " current-page-name " of " total-pages ". click to jump or search")
             #_:on-key-down #_#(let [k (.-key %)]
                             (case k
                               "ArrowLeft" (do (.stopPropagation %) (change-page! :prev))
                               "ArrowRight" (do (.stopPropagation %) (change-page! :next))
                               nil))
             :on-click #(do (.preventDefault %) (.stopPropagation %) (open-jumper!))}
            current-page-name "/" total-pages]

           (button "›" (str "next-page-" (inc current-page-name)) #(do (when (== last-page current-page-name) (focus-page!))
                                                                       (change-page! :next))
                   (== current-page last-page))
           (when (< 9 last-page)
             (button "»" "last-page" #(do (focus-page!) (change-page! last-page)) (== current-page last-page)))
           ]))})))
