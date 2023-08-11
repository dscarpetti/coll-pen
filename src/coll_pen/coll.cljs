(ns coll-pen.coll
  (:require
   ;;[clojure.pprint :refer [pprint]]
   [clojure.string :as str]
   [coll-pen.edit :as edit]
   [coll-pen.input :as input]
   [coll-pen.delim :as delim]
   [coll-pen.paginator :as page]
   [reagent.core :as r]))

(declare draw-el)

(defn aria-description [v]
  (cond
    (nil? v) "nil"
    (string? v) "string"
    (keyword? v) "keyword"
    (symbol? v) "symbol"
    (number? v) "number"
    (boolean? v) "boolean"
    (coll? v) (cond (vector? v) "vector"
                    (set? v) "set"
                    (map? v) "map"
                    :else "sequence")
    :else (type v)))

(defn draw-value [config t v]
  (cond
    (keyword? v) [:span.coll-pen-value.coll-pen-value-keyword
                  {:class (str "coll-pen-value-" t)}
                  (if-let [ns (namespace v)]
                    [:span [:span ":"] [:span.coll-pen-value-namespace ns] [:span (name v)]]
                    (str v))]
    (symbol? v) [:span.coll-pen-value.coll-pen-value-symbol
                 {:class (str "coll-pen-value-" t)}
                 (if-let [ns (namespace v)]
                   [:span [:span.coll-pen-value-namespace ns] [:span (name v)]]
                   (str v))]
    :else (let [sv (pr-str v)
                sv (if-let [truncate (:truncate config)]
                     (if (<= (count sv) truncate)
                       sv
                       (str (str/trim (subs sv 0 truncate)) "…"))
                     sv)]
            [:span.coll-pen-value
             {:class (str "coll-pen-value-" t " coll-pen-value-"
                          (cond
                            (string? v) "string"
                            ;;(keyword? v) "keyword"
                            ;;(symbol? v) "symbol"
                            (number? v) "number"
                            (coll? v) "coll"
                            :else "other"))}
             sv])))

(defn draw-map-key [config k]
  (draw-value config "map-key" k))

(defn draw-vec-idx [idx]
  [:span.coll-pen-value.coll-pen-value-vec-idx.coll-pen-value-number
   {:aria-label (str "index " idx)}
   idx])

(defn draw-leaf [config el]
  (if-let [custom-leaf-renderer (config :custom-leaf-renderer)]
    (let [res (custom-leaf-renderer el)]
      (if (nil? res)
        (draw-value config "leaf" el)
        [:span.coll-pen-value res]))
    (draw-value config "leaf" el)))

(defn draw-direct-leaf [config el]
  (draw-value config "leaf" el))

(defn enter-click [e]
  (when (= "Enter" (.-key e))
    (.click (.-target e))))

(def ^:const coll-name {:map "map" :vec "vector" :set "set" :seq "sequence"})

(defn draw-loading-coll [react-key coll-type delim-color on-click aria-label]
  (let [el (atom nil)]
    (r/create-class
     {:display-name "draw-loading-coll"
      :component-did-mount #(input/focus-on-mount el react-key)
      :reagent-render
      (fn [react-key coll-type delim-color on-click aria-label]
        [:span.coll-pen-el.coll-pen-collapsed {:ref #(reset! el %)
                                                         :key react-key :on-click on-click :on-key-press enter-click
                                                         :aria-label (str "loading " aria-label) :aria-expanded true
                                                         :title "clear loading animation" :tab-index "0" :role "button"}
         [:span.coll-pen-coll
          (delim/open coll-type delim-color "5")
          [:span.coll-pen-handle "×"]
          [:span.coll-pen-el [:span.coll-pen-spinner]]
          (delim/close coll-type delim-color "5")]])})))

(defn draw-collapsed-coll [react-key coll-type delim-color on-click content aria-label]
  (let [el (atom nil)]
    (r/create-class
     {:display-name "draw-collapsed-coll"
      :component-did-mount #(input/focus-on-mount el react-key)
      :reagent-render
      (fn [react-key coll-type delim-color on-click content aria-label]
        [:span.coll-pen-el.coll-pen-collapsed {:ref #(reset! el %)
                                                         :key react-key :on-click on-click :on-key-press enter-click
                                                         :aria-label (str aria-label " estimated size is " content)
                                                         :aria-expanded false
                                                         :title "expand" :tab-index "0" :role "button"}
         [:span.coll-pen-coll
          (delim/open coll-type delim-color "5")
          [:span.coll-pen-handle "+"]
          [:span.coll-pen-el [:span.coll-pen-value-collapsed content]]
          (delim/close coll-type delim-color "5")]])})))

(defn draw-empty-coll [react-key config coll-type delim-color on-click aria-label original-coll path]
  (let [el (atom nil)
        edit-handler (config :edit-handler)]
    (r/create-class
     {:display-name "draw-empty-coll"
      :component-did-mount #(input/focus-on-mount el react-key)
      :reagent-render
      (fn [react-key config coll-type delim-color on-click aria-label original-coll path]
        [:span.coll-pen-el {:key react-key}
         [:span.coll-pen-coll.coll-pen-empty
          [:span.coll-pen-handle
           {:ref #(reset! el %)
            :title "collapse empty"
            :tab-index "0" :role "button"
            :aria-label aria-label :aria-expanded true
            :on-click on-click :on-key-press enter-click}
           "−"]
          (delim/open coll-type delim-color "5")
          [:span.coll-pen-el [:span.coll-pen-empty-placeholder]]
          (when (and edit-handler (not (keyword-identical? coll-type :seq)))
            [edit/value-adder edit-handler config (str react-key "_") original-coll path coll-type #()])
          (delim/close coll-type delim-color "5")]])})))

(defn recursively-check-loaded-status [init-states parent-coll parent-path k]
  (let [coll (get parent-coll k)]
    (when (coll? coll)
      (let [path (conj parent-path k)
            init-state (get init-states path)]
        (if (or (not (:loaded init-state)) (:loading init-state))
          path
          (when (or (map? coll) (vector? coll))
            (let [ks (if (map? coll)
                       (keys coll)
                       (range (count coll)))
                  res (reduce (fn [x k]
                                (when-let [p (recursively-check-loaded-status init-states coll path k)]
                                  (reduced p)))
                              nil ks)]
              (when-not (nil? res)
                (if (reduced? res)
                  @res
                  res)))))))))

(def focus-entry (r/atom nil))

(defn draw-entry [edit-handler config react-key coll path coll-type k v]
  (let [is-set (keyword-identical? :set coll-type)
        focus-el (atom nil)
        init-states (:init-states config)
        focus-key (str react-key "*" k)
        editor-react-key (str react-key "+" k)
        success (r/atom nil)
        set-success! #(reset! success [(str (gensym "success")) %])
        clear-success! #(reset! success nil)

        error (r/atom (get-in @init-states [path [k :error]]))
        clear-error! #(do (when % (.stopPropagation %))
                          (swap! init-states update path dissoc [k :error])
                          (reset! error nil)
                          (when % (.focus @focus-el)))
        set-error! (fn [err]
                     (swap! init-states assoc-in [path [k :error]] err)
                     (reset! error err))

        editor-open (r/atom (boolean (get-in @init-states [path [coll-type :editor k]])))
        open-editor! (fn []
                       (input/set-global-focus-key! editor-react-key)
                       (swap! init-states assoc-in [path [coll-type :editor k]] true)
                       (reset! editor-open true))
        close-editor! (fn []
                        (swap! init-states update path dissoc [coll-type :editor k])
                        (reset! editor-open false)
                        (when @focus-el (.focus @focus-el)))

        open! (fn [e v]
                (.stopPropagation e)
                (when-not (or (.-altKey e) (.-metaKey e) (.-ctrlKey e))
                  (if-let [missing-path (and (:load-data-fn config)
                                             (not is-set)
                                             (recursively-check-loaded-status @(:init-states config) coll path k))]
                    (set-error! [:span [:div "unloaded child at"] [:div (pr-str missing-path)]])
                    (open-editor!))))

        on-save (fn [coll new-value ok-cb fail-cb]
                  (let [-ok-cb (fn [content] (ok-cb)
                                 (close-editor!)
                                 (clear-error! nil)
                                 (set-success! content)
                                 (when (keyword-identical? :set coll-type)
                                   (reset! focus-entry {path new-value
                                                        :focus-key (str react-key "*" new-value)}))
                                 (input/delay-focus! focus-el))
                        -fail-cb (fn [content] (fail-cb) (set-error! (or content "Error")))]
                    (try
                      (edit-handler {:old-coll coll
                                     :new-coll (if (= coll-type :set) (conj (disj coll k) new-value) (assoc coll k new-value))
                                     :path path
                                     :key k
                                     :old-value v
                                     :new-value new-value}
                                    -ok-cb -fail-cb)
                      (catch :default e
                        (.error js/console e)
                        (-fail-cb "update handler error")))))

        on-delete (fn [coll ok-cb fail-cb]
                    (let [-ok-cb (fn [] (ok-cb) (close-editor!) (clear-error! nil) (set-success! nil) (input/delay-focus! focus-el))
                          -fail-cb (fn [content] (fail-cb) (set-error! (or content "Error")))]
                      (try
                        (edit-handler {:old-coll coll
                                       :new-coll (case coll-type
                                                   :map (dissoc coll k)
                                                   :vec (if (= k (dec (count coll)))
                                                          (pop coll)
                                                          (throw
                                                           (ex-info "can only delete last element from vector"
                                                                    {:vector coll :last-index (dec (count coll)) :delete-index k})))

                                                   :set (disj k))
                                       :path path
                                       :key k
                                       :old-value v
                                       :deleted true}
                                      -ok-cb -fail-cb)
                        (catch :default e
                          (.error js/console e)
                          (-fail-cb "update handler error")))))

        on-cancel (fn [ok-cb]
                    (ok-cb)
                    (close-editor!)
                    (input/delay-focus! focus-el))]

    (r/create-class
     {:display-name "draw-entry"
      :component-did-mount (fn []
                             ;;(println focus-key (get @focus-entry :focus-key))
                             (when (= (get @focus-entry :focus-key) focus-key)
                               (println "FOCUS" focus-key @focus-el)
                               (reset! focus-entry nil)
                               (when @focus-el (.focus @focus-el))))
      :reagent-render
    (fn [edit-handler config react-key coll path coll-type k v]
      (let [editing @editor-open]
        [:span.coll-pen-el
         (when-not (and editing is-set)
           [:span (when edit-handler
                    {:class (when-not editing "coll-pen-editor-button")
                     :ref #(reset! focus-el %) ;:style {:cursor :pointer}
                     :title (str "edit " k) :role "button" :tab-index "0"
                     :aria-label (str "edit " (conj path k)) :aria-expanded editor-open
                     :on-click (when-not editing #(open! % v)) :on-key-press enter-click})
            (case coll-type
              :map (draw-map-key config k)
              :vec (draw-vec-idx k)
              :set (draw-leaf config k))])
         (when-let [error @error]
           (edit/error-alert error clear-error!))
         (cond
           editing [edit/value-editor config editor-react-key coll path coll-type k v on-save on-delete on-cancel]
           (not is-set) (draw-el config v (conj path k)))
         (when-let [success @success]
           (let [[k msg] success]
             [edit/success-floater k msg clear-success!]))

         ]))})))

(defn draw-data-coll [react-key config coll path coll-type delim-color on-click pagination status-line vec-offset aria-label
                      original-coll]
  (let [el (atom nil)
        edit-handler (config :edit-handler)]
    (r/create-class
     {:display-name "draw-data-coll"
      :component-did-mount #(input/focus-on-mount el react-key)
      :reagent-render
      (fn [react-key config coll path coll-type delim-color on-click pagination status-line vec-offset aria-label
           original-coll]
        (let [width (when (== 1 (count coll)) "5")]
          [:span.coll-pen-coll
           {:key react-key
            :class (case coll-type :map "coll-pen-map" :vec "coll-pen-vec" :set "coll-pen-set" "coll-pen-seq")}
           [:span.coll-pen-handle {:ref #(reset! el %)
                                        :title "collapse" :tab-index "0" :role "button"
                                        :aria-label aria-label :aria-expanded true
                                        :on-click on-click :on-key-press enter-click} "−"]
           (delim/open coll-type delim-color width)
           pagination
           status-line
           (case coll-type
             :map (doall (map (fn [[k v]] ^{:key (str coll-type k)}
                                [draw-entry edit-handler config react-key original-coll path coll-type k v]) coll))
             :vec (if (vector? coll)
                    (doall (map-indexed (fn [i v] ^{:key (str coll-type (+ i vec-offset))}
                                          [draw-entry edit-handler config react-key original-coll path coll-type (+ i vec-offset) v])
                                        coll))
                    (doall (map (fn [[i v]] ^{:key (str coll-type i)}
                                  [draw-entry edit-handler config react-key original-coll path coll-type i v])
                                coll)))
             :set (doall (map (fn [v] ^{:key (str coll-type v)}
                                [draw-entry edit-handler config react-key original-coll path coll-type v v]) coll))
             (doall (map-indexed (fn [i v] [:span.coll-pen-el {:key (str coll-type "-" i)} (draw-leaf config v)]) coll)))
           (when (and edit-handler (not (keyword-identical? coll-type :seq)))
             [edit/value-adder edit-handler config (str react-key "_") original-coll path coll-type
              #(reset! focus-entry {path %})])
           (delim/close coll-type delim-color width)]))})))

(defn draw-coll [config coll path]
  (let [coll-type (cond (map? coll) :map (vector? coll) :vec (set? coll) :set :else :seq)

        react-key (reduce #(str %1 " " (aria-description %2) " " %2) "" path ) #_(pr-str path)

        init-states (:init-states config)
        init-state (dissoc (get @init-states path) :key-vec)

        local-state (r/atom init-state)

        get-collapsed-content (:get-collapsed-content config)

        el-per-page (:el-per-page config)

        change-page! (fn [k coll current-page loc]
                       (let [last-page (quot (dec (count coll)) el-per-page)
                             new-page (cond
                                        (keyword-identical? loc :next) (min last-page (inc current-page))
                                        (keyword-identical? loc :prev) (max 0 (dec current-page))
                                        (keyword-identical? loc :last) last-page
                                        (number? loc) (min last-page (max 0 loc))
                                        :else 0)]
                         (swap! init-states assoc-in [path k] new-page)
                         (swap! local-state assoc k new-page)))

        load-data-fn (:load-data-fn config)
        ;;disable-loading-animation false;;(:disable-loading-animation config)

        loaded-callback (fn callback [sub-paths]
                          (doseq [sub-path sub-paths]
                            (when (coll? sub-path)
                              (let [p (into path sub-path)]
                                (swap! init-states
                                         #(-> %
                                            (assoc-in [p :loaded] true)
                                            (update p dissoc :loading)
                                            (assoc-in [p :expanded] true))))))

                          (input/set-global-focus-key! react-key)
                          (swap! init-states update path dissoc :loading)
                          (swap! local-state dissoc :loading))

        on-click (if load-data-fn
                   (fn [e]
                     (.stopPropagation e)
                     (input/set-global-focus-key! react-key)
                     (if (:loaded @local-state)
                       (do
                         (swap! init-states update-in [path :expanded] not)
                         (swap! local-state update :expanded not))
                       (do
                         (swap! init-states update path merge {:expanded true :loaded true :loading true
                                                               #_(not disable-loading-animation)})
                         (swap! local-state assoc :expanded true :loaded true :loading true #_(not disable-loading-animation))
                         (load-data-fn coll path loaded-callback))))
                   (fn [e]
                     (.stopPropagation e)
                     (input/set-global-focus-key! react-key)
                     (swap! init-states update-in [path :expanded] not)
                     (swap! local-state update :expanded not)))

        search-handler (:search-handler config)
        search-instructions (:search-instructions config)
        run-search (if search-handler
                     (fn [coll search-string]
                       (let [res (search-handler coll search-string)]
                         (if (string? res)
                           [nil res]
                           [res nil])))
                     (fn [coll search-string]
                       [nil "No Search Handler"]))

        set-search! (fn [s]
                      (swap! init-states assoc-in [path :search] s)
                      (swap! local-state assoc :search s))

        open-search! (when search-handler
                       (fn [s]
                         (input/set-global-focus-key! react-key)
                         (swap! init-states assoc-in [path :search] "")
                         (swap! local-state assoc :search "")))

        clear-search! (fn []
                        (input/set-global-focus-key! react-key)
                        (swap! init-states update path dissoc :search :search-page)
                        (swap! local-state dissoc :search :search-page))



        close-search! (fn []
                        (let [reset-fn (fn [{:keys [jump-reset] :as s}]
                                         (-> s
                                             (assoc :current-page jump-reset)
                                             (dissoc :jump-reset :search :search-page)))]
                          (input/set-global-focus-key! (str react-key "-page"))
                          (swap! init-states update path reset-fn)
                          (swap! local-state reset-fn)))

        open-jumper! (fn [reset-page]
                       (input/set-global-focus-key! react-key)
                       (swap! init-states assoc-in [path :jump-reset] reset-page)
                       (swap! local-state assoc :jump-reset reset-page))

        close-jumper! (fn [reset?]
                        (input/set-global-focus-key! (str react-key "-page"))
                        (if reset?
                          (let [reset-fn (fn [{:keys [jump-reset] :as s}]
                                           (-> s
                                               (assoc :current-page jump-reset)
                                               (dissoc :jump-reset)))]
                            (swap! init-states update path reset-fn)
                            (swap! local-state reset-fn))
                          (do
                            (swap! init-states update path dissoc :jump-reset)
                            (swap! local-state dissoc :jump-reset))))
        delim-color ((:get-delim-color config) path)

        aria-label (str (coll-name coll-type) " at " react-key)]
    (r/create-class
     {:display-name "draw-coll"
      ;; :component-did-mount (fn []
      ;;                        (println (:default-expanded config) (nil? (:expanded @local-state)))
      ;;                        (when (and (:default-expanded config) (nil? (:expanded @local-state)))
      ;;                          (swap! init-states assoc-in [path :expanded] true)
      ;;                          (swap! local-state assoc :expanded true)))
      :UNSAFE_component-will-receive-props
      (fn [x [_ _ coll path]]
        (when-let [fk (get @focus-entry path)]
          (let [current-page @local-state
                index (if (keyword-identical? :vec coll-type)
                        fk
                        (let [keys (case coll-type
                                     :map (mapv first (seq coll))
                                     :set (vec (seq coll)))]
                          (loop [i (long 0)]
                            (when (< i (count keys))
                              (if (= (keys i) fk)
                                i
                                (recur (inc i)))))))

                new-page (quot index el-per-page)]
            (if (= current-page new-page)
              (reset! focus-entry nil)
              (do
                (swap! focus-entry dissoc path)
                (change-page! :current-page coll current-page new-page))))))
      :reagent-render
      (fn [config coll path]
        (let [{:keys [expanded current-page loading jump-reset search search-page]} @local-state
              expanded (or expanded (and (nil? expanded) (:default-expanded config)))
              len (count coll)]
          (cond
            (not expanded) [draw-collapsed-coll react-key coll-type delim-color on-click (get-collapsed-content coll) aria-label]

            loading [draw-loading-coll react-key coll-type delim-color #(swap! local-state dissoc :loading) aria-label]

            (== 0 len) [draw-empty-coll react-key config coll-type delim-color on-click aria-label coll path]

            (<= len el-per-page) [draw-data-coll react-key config coll path coll-type delim-color on-click nil nil 0 aria-label coll]

            search (let [paginator [page/search-paginator react-key search search-page set-search! clear-search! close-search!]
                         [res error] (when (pos? (count search)) (run-search coll search))
                         res-len (count res)
                         status-line (cond
                                       (== 0 (count search)) [:div.coll-pen-search-instructions search-instructions]
                                       error [:div.coll-pen-search-error error]
                                       :else [:div.coll-pen-search-status "found " res-len "/" len])]
                     (if (< res-len el-per-page)
                       [draw-data-coll react-key config (seq res) path coll-type delim-color on-click paginator status-line 0 aria-label
                        coll]

                       (let [last-page (quot (dec res-len) el-per-page)
                             current-page (min last-page (or search-page 0))
                             from-idx (* search-page el-per-page)
                             to-idx (min (+ from-idx el-per-page) res-len)
                             draw-res (if (vector? res)
                                   (seq (subvec res from-idx to-idx))
                                   (do (take el-per-page (drop from-idx res))))
                             status-line [page/base-paginator react-key res el-per-page current-page last-page
                                          #(change-page! :search-page res current-page %) #() open-search!]]
                         [draw-data-coll react-key config draw-res path coll-type delim-color on-click paginator status-line from-idx
                          aria-label coll]
                         )))
            :else (let [last-page (quot (dec len) el-per-page)
                        current-page (min last-page (or current-page 0))
                        from-idx (* current-page el-per-page)
                        to-idx (min (+ from-idx el-per-page) len)
                        draw-coll (if (vector? coll)
                                    (subvec coll from-idx to-idx)
                                    (do (take el-per-page (drop from-idx coll))))
                        paginator (if jump-reset
                                    [page/jump-paginator react-key current-page
                                     #(change-page! :current-page coll current-page %) close-jumper! open-search!]
                                    [page/base-paginator react-key coll el-per-page current-page last-page
                                     #(change-page! :current-page coll current-page %) open-jumper! open-search!])]
                    [draw-data-coll react-key config draw-coll path coll-type delim-color on-click paginator nil from-idx aria-label
                     coll]
                    ))))})))

(defn draw-el [config el path]
  (if-let [custom-renderer (:custom-renderer config)]
    (let [res (custom-renderer el)]
      (if (nil? res)
        (if (coll? el)
          [draw-coll config el path]
          (draw-direct-leaf config el))
        [:div.coll-pen-value res]))
    (if (coll? el)
      [draw-coll config el path]
      (draw-direct-leaf config el))))
