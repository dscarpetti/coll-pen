(ns coll-pen.edit
  (:require
   [cljs.reader]
   [clojure.pprint :refer [pprint]]
   [clojure.string :as str]
   [coll-pen.input :as input]
   [reagent.core :as r]))


(defn longest-line [val max-size min-size]
  (let [len (min max-size (+ 1 (reduce max min-size (map count (str/split val #"\n")))))]
    (if (or (js/isNaN len) (== 0 len))
      min-size
      len)))

(defn count-lines [s max-line-size]
  (-> s
      (str/replace (re-pattern (str "[^\n]{"max-line-size"}")) "\n")
      (str/replace #"[^\n]" "")))

(defn num-rows [val max-size min-size max-line-size]
  (let [len (min max-size (inc (count (count-lines val max-line-size))))]
    ;;(prn val)
    ;;(prn [val (str/replace (count-lines val max-line-size) #"\n" "_")])
    (if (or (js/isNaN len) (== 0 len))
      min-size
      (if (< 1 len)
        (inc len)
        len))))

(defn enter-click [e]
  (when (= "Enter" (.-key e))
    (.click (.-target e))))

(defn error-alert
  ([content clear-fn]
   (when content
     [:span.coll-pen-alert.coll-pen-error
      {:aria-role :alert
       :tabindex "0"
       :style {:cursor :pointer}
       :title "clear alert"
       :on-click clear-fn
       :on-key-press enter-click}
      content]))
  ([content clear-fn div]
   (when content
     [:div.coll-pen-alert.coll-pen-error
      {:aria-role :alert
       :tabindex "0"
       :style {:cursor :pointer}
       :title "clear alert"
       :on-click clear-fn
       :on-key-press enter-click}
      content])))

(defn success-alert [content clear-fn]
  (when content
    [:span.coll-pen-alert.coll-pen-success
     {:aria-role :alert
      :tabindex "0"
      :style {:cursor :pointer}
      :title "clear alert"
      :on-click clear-fn
      :on-key-press enter-click}
     content]))

(def +invalid+ {:class "coll-pen-invalid"
                :validated nil
                :delete false
                :is-changed true
                :is-valid false})

(defn validate [s original-val delete-on-empty]
  (or
   (try
     (if (empty? (str/trim s))
       (when delete-on-empty
         {:class "coll-pen-value-other"
          :validated nil
          :delete true
          :is-changed true
          :is-valid true})
       (when (zero? (reduce (fn [n c]
                              (if (= \[ c)
                                (inc n)
                                (if (zero? n)
                                  (reduced -1)
                                  (dec n))))
                            0 (str/replace s #"[^\[\]]" "")))
         (let [x (cljs.reader/read-string (str "["s "]"))]
           (when (== 1 (count x))
             (let [validated (x 0)]
               ;;(prn [validated original-val (= validated original-val)])
               {:class (cond
                         (string? validated) "coll-pen-value-string"
                         (keyword? validated) "coll-pen-value-keyword"
                         (symbol? validated) "coll-pen-value-symbol"
                         (number? validated) "coll-pen-value-number"
                         (coll? validated) "coll-pen-value-coll"
                         :else "coll-pen-value-other")
                :validated validated
                :is-changed (not (= validated original-val))
                :delete false
                :is-valid true})))))
     (catch :default e))
   +invalid+))

(defn button
  ([label title on-click disabled class]
   [:a.coll-pen-button.coll-pen-noselect
    {:class (if disabled "coll-pen-disabled" class)
     :href (when-not disabled (str "#" title)) :title title :aria-label title :role "button"
     :on-click (when-not disabled #(do (.preventDefault %) (.stopPropagation %) (on-click)))}
    label])
  ([label title on-click disabled class ref-fn]
   [:a.coll-pen-button.coll-pen-noselect
    {:class (if disabled "coll-pen-disabled" class)
     :ref ref-fn
     :href (when-not disabled (str "#" title)) :title title :aria-label title :role "button"
     :on-click (when-not disabled #(do (.preventDefault %) (.stopPropagation %) (on-click)))}
    label]))


(defn editor-text-area [focus-key data original-val merge-change! delete-on-empty cols placeholder auto-size]
  (let [input-el (atom nil)
        validation-timeout (atom nil)
        ch-width (atom nil)
        min-width (r/atom 10)
        handle-change (fn [e]
                        (let [s (-> e (.-target) (.-value))]
                          (when-let [t @validation-timeout]
                            (js/clearTimeout t))
                          (reset! validation-timeout (js/setTimeout #(merge-change! (validate s original-val delete-on-empty)) 50))
                          (merge-change! {:raw s})))]
    (r/create-class
     {:display-name "editor-text-area"
      :component-did-mount #(let [el @input-el]
                              (input/focus-on-mount input-el focus-key)
                              (when auto-size
                                (try
                                  (let [parent-el (.-parentElement el)
                                        parent-width (inc (.-width (.getBoundingClientRect parent-el)))
                                        measure (.createElement js/document "div")
                                        ch (do (set! (.-className measure) "coll-pen-textarea-measure")
                                               (.appendChild parent-el measure)
                                               (.-width (.getBoundingClientRect measure)))]
                                    (.removeChild parent-el measure)
                                    (reset! ch-width ch)
                                    (if (< (* ch @min-width) parent-width)
                                      (reset! min-width (js/Math.ceil (/ parent-width ch)))))
                                  (catch :default e))))
      :component-did-update #(input/focus-on-mount input-el focus-key)
      ;; :component-did-update #(let [el @input-el
      ;;                              ch @ch-width
      ;;                              parent-width (.-width (.getBoundingClientRect (.-parentElement el)))]
      ;;                          (if (< (* ch (inc @min-width)) parent-width)
      ;;                            (reset! min-width (js/Math.ceil (/ parent-width ch)))))
      :reagent-render
      (fn [focus-key data original-coll merge-change! delete-on-empty cols placeholder auto-size]
        (let [val (:raw data)
              min-width @min-width
              max-width (max min-width 50)]
          [:textarea
           {:value val
            :ref #(reset! input-el %)
            :placeholder placeholder
            :class (:class data)
            :spell-check "false"
            :auto-complete "false"
            :cols (or cols (longest-line val max-width min-width))
            :rows (num-rows val 20 1 max-width)
            :on-change handle-change}]))})))

(defn editor-controls [on-save on-delete on-cancel]
  [:div.coll-pen-editor-controls.coll-pen-coll-paginator
   (button "×" "cancel" on-cancel false nil)
   (if on-delete
     (button "⌫" "delete" on-delete false "coll-pen-delete")
     (if on-save
       (button "✓" "save" on-save false "col-pen-save")
       (button "✓" "save" #() true nil)))])

(defn pprint-str [x]
  (str/trim (with-out-str (pprint x))))

(defn value-editor [config focus-key coll path coll-type k init-val on-save on-delete on-cancel]
  (let [init-states (:init-states config)
        init-state-key [coll-type :edit k]
        init-val-str (pprint-str init-val)
        delete-on-empty (or (not (keyword-identical? :vec coll-type))
                            (= (dec (count coll)) k))
        original-val (get coll k)
        data (if-let [existing-state (get-in @init-states [path init-state-key])]
               (r/atom existing-state)
               (let [new-state (assoc (validate init-val-str original-val delete-on-empty) :raw init-val-str)]
                 (swap! init-states assoc-in [path init-state-key] new-state)
                 (r/atom new-state)))
        merge-change! (fn [new-data]
                        (swap! init-states update-in [path init-state-key] merge new-data)
                        (swap! data merge new-data))
        clear-init-state! #(swap! init-states update path dissoc init-state-key)
        start-waiting! #(do (swap! init-states assoc-in [path init-state-key :waiting] true)
                            (swap! data assoc :waiting true))
        clear-waiting! #(do (swap! init-states update-in [path init-state-key] dissoc :waiting)
                            (swap! data dissoc :waiting))
        build-fail-handler (fn []
                             (let [revert-data (dissoc @data :waiting)]
                               (fn []
                                 (swap! init-states assoc-in [path init-state-key] revert-data)
                                 (reset! data revert-data))))
        on-save (fn [coll]
                  (let [raw (:raw @data)
                        {:keys [is-valid delete validated is-changed]} (validate raw original-val delete-on-empty)]
                    (if (not is-changed)
                      (on-cancel clear-init-state!)
                      (when (and is-valid (not delete))
                        (start-waiting!)
                        (on-save coll validated clear-init-state! (build-fail-handler))))))
        kb-delete (fn [coll]
                    (when delete-on-empty
                      (start-waiting!)
                      (on-delete coll clear-init-state! (build-fail-handler))))
        on-delete (fn [coll]
                    (let [raw (:raw @data)
                          {:keys [is-valid delete]} (validate raw original-val delete-on-empty)]
                      (when (and is-valid delete)
                        (start-waiting!)
                        (on-delete coll clear-init-state! (build-fail-handler)))))

        on-cancel (fn [] (on-cancel clear-init-state!))]
    (fn [config focus-key coll path coll-type k init-val _ _ _]
      (let [{:keys [raw validated is-valid is-changed class delete waiting] :as val-data} @data]
        [:div.coll-pen-waiting-outer
         (when waiting
           [:div.coll-pen-waiting-inner {:on-click #(.stopPropagation %) :on-key-down #(.stopPropagation %)}
            [:div.coll-pen-waiting-cover]
            [:div.coll-pen-waiting-positioner
             [:div.coll-pen-waiting-value [:div.coll-pen-spinner #_{:on-click clear-waiting!}]]]])
         [:div.coll-pen-editor-field
          {:class (cond
                    (not is-changed) "coll-pen-unchanged"
                    (not is-valid) "coll-pen-invalid")
           :on-key-down (fn [e]
                          (case (.-key e)
                            "Escape" (on-cancel)
                            "Enter" (when (or (.-metaKey e) (.-ctrlKey e)) (on-save coll))
                            "Backspace"(when (or (.-metaKey e) (.-ctrlKey e)) (kb-delete coll))
                            "Delete" (when (or (.-metaKey e) (.-ctrlKey e)) (kb-delete coll))
                        nil))}
          [editor-text-area focus-key val-data original-val merge-change! delete-on-empty nil "" false]
          [editor-controls (when is-valid #(on-save coll)) (when (and is-valid delete) #(on-delete coll)) on-cancel]]]))))



(defn success-floater [k msg clear!]
  (let [timeout (atom nil)]
    (r/create-class
     {:display-name "success-floater"
      :component-did-mount #(reset! timeout (js/setTimeout clear! 3000))
      :component-did-update #(do
                               (when-let [t @timeout]
                                 (js/clearTimeout t))
                               (reset! timeout (js/setTimeout clear! 3000)))
      :reagent-render
      (fn [k msg clear!]
        (let [message (or msg "✓")]
          [:div.coll-pen-success-floater {:key k
                                          :aria-role "alert"
                                          :aria-label (or msg "success")
                                          :style {:right (str (count message) "ch")}
                                          } message ]))})))

(defn success-floater-bar [k msg clear!]
  (let [timeout (atom nil)]
    (r/create-class
     {:display-name "success-floater"
      :component-did-mount #(reset! timeout (js/setTimeout clear! 3000))
      :component-did-update #(do
                               (when-let [t @timeout]
                                 (js/clearTimeout t))
                               (reset! timeout (js/setTimeout clear! 3000)))
      :reagent-render
      (fn [k msg clear!]
        (let [message (or msg "✓")]
          [:div.coll-pen-success-floater-bar {:key k
                                              :aria-role "alert"
                                              :aria-label (or msg "success")
                                              } message ]))})))


(defn value-adder [edit-handler config focus-key coll path coll-type set-focus!]
  (let [is-map (keyword-identical? :map coll-type)
        focus-el (atom nil)
        init-states (:init-states config)
        init-state-key [coll-type :add]
        input-focus-key (if is-map
                          (str focus-key "key")
                          (str focus-key "val"))

        data (r/atom (get-in @init-states [path init-state-key]))

        success (r/atom nil)
        set-success! #(reset! success [(str (gensym "success")) %])
        clear-success! #(reset! success nil)

        error (r/atom (get-in @init-states [path [coll-type :add :error]]))
        clear-error! #(do (when % (.stopPropagation %))
                          (swap! init-states update path dissoc [coll-type :add :error])
                          (reset! error nil)
                          (if @data
                            (do
                              (input/set-global-focus-key! input-focus-key)
                              (swap! data (fn [d]
                                            (let [nonce (rand)]
                                              (-> d
                                                  (assoc-in [:key :nonce] nonce)
                                                  (assoc-in [:val :nonce] nonce))))))
                            (input/delay-focus! focus-el)))
        set-error! (fn [err]
                     (swap! init-states assoc-in [path [coll-type :add :error]] err)
                     (reset! error err))

        start-waiting! #(do (swap! init-states assoc-in [path init-state-key :waiting] true)
                            (swap! data assoc :waiting true))
        clear-waiting! #(do (swap! init-states update-in [path init-state-key] dissoc :waiting)
                            (swap! data dissoc :waiting))

        open-editor! #(let [new-data {:key (if is-map
                                             (assoc (validate "" nil false) :raw "")
                                             {:is-valid true})
                                      :val (assoc (validate "" nil false) :raw "")}]
                        (input/set-global-focus-key! input-focus-key)
                        (swap! init-states assoc-in [path init-state-key] new-data)
                        (reset! data new-data)
                        (when % (.stopPropagation %)))

        close-editor! #(do
                         (swap! init-states update path dissoc init-state-key)
                         (reset! data nil)
                         (input/delay-focus! focus-el))

        on-save (fn [coll]
                  (let [{:keys [key val] :as revert-data} @data
                        raw-key (:raw key)
                        raw-val (:raw val)
                        val (validate raw-val ::nothing false)
                        new-val (:validated val)
                        key (case coll-type
                              :map (validate raw-key ::nothing false)
                              :vec {:validated (count coll) :is-valid true}
                              :set {:validated new-val :is-valid true})
                        new-key (:validated key)
                        is-valid (and (:is-valid key) (:is-valid val))]
                    (when is-valid
                      (let [ok-cb (fn [content] (close-editor!) (clear-waiting!) (clear-error! nil) (set-success! content) (set-focus! new-key))
                            fail-cb (fn [content]
                                      (swap! init-states assoc-in [path init-state-key] revert-data)
                                      (input/set-global-focus-key! input-focus-key)
                                      (reset! data revert-data)
                                      (set-error! (or content "Error")))
                            new-coll (case coll-type
                                       :map (assoc coll new-key new-val)
                                       :vec (conj coll new-val)
                                       :set (conj coll new-val))]
                        (start-waiting!)
                        (try
                          (edit-handler {:old-coll coll
                                         :new-coll new-coll
                                         :path path
                                         :create true
                                         :key new-key
                                         :value new-val}
                                        ok-cb fail-cb)
                          (catch :default e
                            (.error js/console e)
                            (fail-cb "update handler error")))))))
        merge-key! (fn [new-data]
                        (swap! init-states update-in [path init-state-key :key] merge new-data)
                     (swap! data update :key merge new-data))
        merge-val! (fn [new-data]
                     (swap! init-states update-in [path init-state-key :val] merge new-data)
                     (swap! data update :val merge new-data))

        ]
    (fn [edit-handler config focus-key coll path coll-type set-focus!]
      (let [{:keys [key val waiting] :as expanded} @data
            is-valid (and (:is-valid key) (:is-valid val))]
        [:div.coll-pen-waiting-outer
         (when waiting
           [:div.coll-pen-waiting-inner {:on-click #(.stopPropagation %) :on-key-down #(.stopPropagation %)}
            [:div.coll-pen-waiting-cover]
            [:div.coll-pen-waiting-positioner
             [:div.coll-pen-waiting-value [:div.coll-pen-spinner #_{:on-click clear-waiting!}]]]])
         (when-let [success @success]
           (let [[k msg] success]
             [success-floater-bar k msg clear-success!]))
         (when-let [error @error]
           (error-alert error clear-error! true))
         (if expanded
           [:div.coll-pen-editor-field
            {:class (when-not is-valid "coll-pen-invalid")
             :on-key-down #(cond
                             (and (= (.-key %) "Enter") (or (.-metaKey %) (.-ctrlKey %))) (on-save coll)
                             (= (.-key %) "Escape") (close-editor!))}
            (when is-map
              [:div.coll-pen-editor-frame
               [editor-text-area (str focus-key "key") key ::nothing merge-key! false nil "key" true]])
            [editor-text-area (str focus-key "val") val ::nothing merge-val! false nil "value" true]
            [editor-controls (when is-valid #(on-save coll)) nil close-editor!]]
           [:div.coll-pen-editor-field.coll-pen-adder
            [:div.coll-pen-editor-controls.coll-pen-coll-paginator
             ;{:ref #(reset! focus-el %)}
             (button "+" "create"  open-editor! false "col-pen-add" #(reset! focus-el %))
             #_(when (keyword-identical? coll-type :vec)
               (button "−" "pop" #() (empty? coll) "col-pen-remove"))]])]))))
