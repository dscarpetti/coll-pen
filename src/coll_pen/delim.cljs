(ns coll-pen.delim)

(defn open-paren [height color & [width]]
  [:svg {:height height :width (or width "10") :view-box "28 0 90 300" :preserve-aspect-ratio "none"}
   ;;[:rect {:stroke "red" :x "0" :y "0" :width "1000" :height "3000"}]
    [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
            :d "M100,300 C30,220 30,80 100,0"}]])

(defn close-paren [height color & [width]]
  [:svg {:height height :width (or width "10") :view-box "82 0 90 300" :preserve-aspect-ratio "none"}
   ;;[:rect {:stroke "red" :x "0" :y "0" :width "1000" :height "3000"}]
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
           :d "M100,300 C170,220 170,80 100,0"}]])

(defn open-curly [height color & [width]]
  [:svg {:height height :width (or width "10") :view-box "-100 0 50 200" :preserve-aspect-ratio "none"}
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
           :d "M-50,0 C-140,10 -25,100 -100,100 M-100,100 C-30,100 -140,180 -50,200"}]])

(defn close-curly [height color & [width]]
  [:svg {:height height :width (or width "10") :view-box "50 0 50 200" :preserve-aspect-ratio "none"}
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
           :d "M50,0 C140,10 25,100 100,100 M100,100 C30,100 140,180 50,200"}]])

(defn open-square [height color & [width]]
  [:svg {:height height :width (or width "10") :view-box "0 0 100 100" :preserve-aspect-ratio "none"}
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
           :d "M80,1 l -60 0 l 0 98 l 60 0"}]])

(defn close-square [height color & [width]]
  [:svg {:height height :width (or width "10") :view-box "0 0 100 100" :preserve-aspect-ratio "none"}
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
            :d "M20,1 l 60 0 l 0 98 l -60 0"}]])

(defn open-angle [height color & [width]]
  [:svg {:height height :width (or width "10") :view-box "0 0 100 100" :preserve-aspect-ratio "none"}
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
           :d "M80,1 l -60 49 l 60 49"}]
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
           :d "M45,49.3 l -45 0"}]
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
           :d "M45,50.7 l -45 0"}]])

(defn close-angle [height color & [width]]
  [:svg {:height height :width (or width "10") :view-box "0 0 100 100" :preserve-aspect-ratio "none"}
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
           :d "M20,1 l 60 49 l -60 49"}]
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
           :d "M55,49.3 l 45 0"}]
   [:path {:stroke color :stroke-width "1" :fill "none" :vector-effect "non-scaling-stroke"
           :d "M55,50.7 l 45 0"}]])

(defn open [type color & [width]]
  [:span.coll-pen-delim.coll-pen-open
   (case type
     :map (open-curly "100%" color width)
     :vec (open-square "100%" color width)
     :set (open-angle "100%" color width)
     :seq (open-paren "100%" color width))])

(defn close [type color & [width]]
  [:span.coll-pen-delim.coll-pen-close
   (case type
     :map (close-curly "100%" color width)
     :vec (close-square "100%" color width)
     :set (close-angle "100%" color width)
     :seq (close-paren "100%" color width))])
