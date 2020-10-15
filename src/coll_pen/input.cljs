(ns coll-pen.input
  (:require
   [reagent.core :as r]))

(def global-focus-key (atom nil))

(defn set-global-focus-key! [k]
  (reset! global-focus-key k))

(defn focus-on-mount [el-atom focus-key]
  (when-let [fk @global-focus-key]
    (when (= focus-key fk)
      (when @el-atom (.focus @el-atom))
      (reset! global-focus-key nil)
      true)))

(defn text [focus-key value on-change keyboard-handlers #_on-enter #_on-escape parse debounce-time tag min-content-width title]
  (let [local-value (r/atom (str value))
        timeout (atom nil)
        dom-el (atom nil)
        clear-timeout! (fn []
                         (when-let [t @timeout]
                           (js/clearTimeout t)))
        debounced-on-change (fn [e]
                              (let [raw-v (-> e (.-target) (.-value))
                                    v (if parse (parse raw-v) raw-v)]
                                ;;(println raw-v "new v" v)
                                (if debounce-time
                                  (do (clear-timeout!)
                                      (reset! timeout (js/setTimeout (fn []
                                                                       (on-change v)
                                                                       (reset! timeout nil))
                                                                     debounce-time)))
                                  (on-change v))
                                (reset! local-value raw-v)))
        on-key-down (fn [e]
                      (let [k (.-key e)
                            f (keyboard-handlers k)]
                        (when f
                          (.preventDefault e)
                          (.stopPropagation e)
                          (clear-timeout!)
                          (f (.-value (.-target e))))))
                        ;;(println k on-enter on-escape)
        #_(cond
                          (and (= k "Enter") on-enter) (do clear-timeout! (on-enter))
                          (and (= k "Escape") on-escape) (do clear-timeout! (on-escape)))
        tag-width (if (empty? tag) 0 (inc (count tag)))
        min-width (+ 1 min-content-width)]
    (r/create-class
     {:display-name "draw-text-input"
      :component-did-mount (fn [] (when-let [fk @global-focus-key]
                                    (when (= fk focus-key)
                                      (.focus @dom-el)
                                      (reset! global-focus-key nil))))
      :reagent-render
      (fn [focus-key value on-change keyboard-handlers #_on-enter #_on-escape parse debounce-time tag min-content-width title]
        (let [local-value @local-value
              width (str (max min-width (+ 1 #_tag-width (count (str local-value)))) "ch")]
          [:span.coll-pen-field
           [:span.coll-pen-input-tag tag]
           [:input {:style {:width width :padding-left (str tag-width "ch")}
                    :title title
                    :aria-label title
                    :type :text
                    :ref (fn [el] (reset! dom-el el))
                    :value local-value
                    :on-blur (:on-blur keyboard-handlers)
                    :on-key-down on-key-down
                    :on-change debounced-on-change}]]))})))

(defn delay-focus!
  ([focus-el-atom]
   (delay-focus! focus-el-atom 8))
  ([focus-el-atom retries]
   ;;(println retries)
   (when (pos? retries)
     (js/setTimeout (fn [] (if @focus-el-atom (.focus @focus-el-atom) (delay-focus! focus-el-atom (dec retries)))) 25))))
