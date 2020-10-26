(ns coll-pen.css)

(defn- build-css-string [{:keys [background foreground
                                 control active disabled
                                 string keyword symbol number other idx status
                                 shadow highlight error
                                 font]}]
  (str "

.coll-pen-reader-label { display: inline-block; width: 0; height: 0; display: none; }
.coll-pen { background-color: "background"; width: max-content; max-width: 100%; }
.coll-pen-value-collapsed { color: "control"; }
.coll-pen-value-string { color: "string"; }
.coll-pen-value-keyword { color: "keyword"; white-space: nowrap;}
.coll-pen-value-namespace { color: "active"; white-space: nowrap; }
.coll-pen-value-namespace::after { content: \"/\" ; color: "foreground" ; }
.coll-pen-value-symbol { color: "symbol";  white-space: nowrap; }
.coll-pen-value-number { color: "number";  white-space: nowrap;}
.coll-pen-value-other { color: "other"; }
.coll-pen-value-coll { color: "status"; }
.coll-pen-value-vec-idx.coll-pen-value-number { color: "idx"; }

.coll-pen.coll-pen-highlight-off *:focus { outline: none; }
.coll-pen.coll-pen-highlight-on *:focus { outline: none; box-shadow: 0px 0px 1px 0px "highlight", inset 0px 0px 1px 0px "highlight"; }
.coll-pen-editor-controls a.coll-pen-button:focus { box-shadow: inset 0px 0px 0px 1px "highlight"; }
.coll-pen-adder .coll-pen-editor-controls a.coll-pen-button:focus { box-shadow: inset 0px 0px 1px 0px "highlight"; }

.coll-pen-editor-field textarea:first-child { border-top-left-radius: 2px; border-top-right-radius: 2px; }

.coll-pen-editor-field textarea { border:none; padding 2px; display: block; min-height: 1.3em;}

.coll-pen-editor-field textarea:focus {box-shadow: inset 0px 0px 0px 1px "highlight"; }
.coll-pen.coll-pen-highlight-on .coll-pen-editor-field textarea:focus {box-shadow: inset 0px 0px 0px 1px "highlight";}

.coll-pen { font-family: "font"; padding: 2ch 1ch; box-sizing: border-box; line-height: 1.1;}
.coll-pen-field input, .coll-pen-editor-field textarea { font-family: "font"; }


.coll-pen-waiting-outer { position: relative; padding-bottom: 3px; padding-top: 2px; padding-left: 3px; padding-right: 3px;}



.coll-pen-waiting-inner { position: absolute; top: 0; bottom: 0; left: 0; right: 0; }
.coll-pen-waiting-cover { position: absolute; top: 0; bottom: 0; left: 0; right: 0; background-color: "background"; border-radius: 2px; opacity: 0.8;}
.coll-pen-waiting-positioner { position: absolute; top: 0; bottom: 0; left: 0; right: 0;
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    justify-content: center;
    align-content: center;
    align-items: center;
    }

.coll-pen-waiting-positioner > * {
    order: 0;
    flex: 0 1 auto;
    align-self: auto;
}

.coll-pen-delim .coll-pen-close, .coll-pen-delim .coll-pen-open { z-index: -2; position: relative; }
.coll-pen-delim svg { position: absolute; top: 0px;}
.coll-pen-delim svg { filter: drop-shadow( 3px 3px 2px "shadow"); }
.coll-pen-delim.coll-pen-open svg { left: -12px; cursor: pointer;}
.coll-pen-delim.coll-pen-close svg { right: -12px; }
.coll-pen-coll { display: inline-block; margin-left: 2ch; position: relative; margin-right: 12px;}
.coll-pen-value-vec-idx, .coll-pen-value-map-key { padding-right: 1ch; display: inline-block;}
.coll-pen-el { display: flex; flex-direction: row; flex-wrap: nowrap;
                    justify-content: flex-start; align-content: center; align-items: center;
                    margin-top: 2px; margin-bottom: 2px; }
.coll-pen-el span { order: 0; flex: 0 1 auto; align-self: auto; }
.coll-pen-empty-placeholder { display: inline-block; height: 1em; width: 0px }
.coll-pen-set .coll-pen-el, .coll-pen-seq .coll-pen-el { justify-content: center; }
.coll-pen-handle { cursor: pointer; position: absolute; top: -3px; bottom: 0; left: -2.7ch; width: 2.8ch; display: inline-block;
                        text-align: left; color: "foreground"; opacity: 0; z-index: 2 }


.coll-pen-alert { text-align: center; padding: 2px; }
.coll-pen-error { color: "error"; font-size: 0.8em;}
.coll-pen-alert:hover { text-shadow: 0px 0px 1px "highlight" ; }
.coll-pen-error * { color: "error";}


.coll-pen-editor-button:hover * { cursor: pointer; color: "control" }
.coll-pen-editor-button:hover .coll-pen-error { color: "error";  }
.coll-pen-editor-button:hover .coll-pen-error * { color: "error";  }

.coll-pen-search-error { text-align: center; color: "error"; padding: 2px; }
.coll-pen-search-instructions { text-align: center; color: "status"; padding: 2px; }
.coll-pen-search-status { text-align: center; color: "status"; padding: 2px; }

.coll-pen-highlight-on .coll-pen-collapsed:focus .coll-pen-handle { opacity: 0.8; }
.coll-pen-highlight-on .coll-pen-handle:focus { opacity: 0.8; }

.coll-pen-handle:hover { opacity: 0.8; }
.coll-pen-collapsed { cursor: pointer; }
.coll-pen-collapsed:hover .coll-pen-handle { opacity: 0.8; }
.coll-pen-collapsed:hover svg { filter: drop-shadow( 0px 0px 2px "highlight"); }
.coll-pen-handle:hover ~ .coll-pen-delim svg  {  filter: drop-shadow( 0px 0px 2px "highlight"); }

.coll-pen-coll-paginator { display: flex; flex-direction: row; flex-wrap: nowrap; justify-content: center; align-content: stretch; align-items: center; color: "control"; text-shadow: 1px 1px 2px "shadow"; }
.coll-pen-coll-paginator .coll-pen-spacer { order: 0; flex: 0 0 1ch; min-width: 1ch; align-self: center; }
.coll-pen-coll-paginator .coll-pen-button { order: 0; flex: 0 0 2ch; min-width: 2ch; align-self: center; text-align: center; }

.coll-pen-coll-paginator .coll-pen-button.coll-pen-button-search { order: 0; flex: 0 0 auto; min-width: 1ch; align-self: center; text-align: center}

.coll-pen-coll-paginator .coll-pen-page { order: 0; flex: 0 1 auto; align-self: auto; }
.coll-pen-coll-paginator .coll-pen-field { order: 0; flex: 0 0 auto; align-self: auto; text-align: center; }

.coll-pen-field input { font-size: 0.9em; color: "control"; background-color: "background"; border: 1px solid "control"; border-radius: 3px; box-shadow: 1px 1px 2px "shadow";}

.coll-pen-editor-controls { border-top: 1px solid "control" ;}
.coll-pen-invalid .coll-pen-editor-controls { border-top: 1px solid "error" ;}
.coll-pen-invalid .coll-pen-editor-controls a.coll-pen-button { border-color: "error" ;}
.coll-pen-editor-controls a.coll-pen-button:hover { border-color: "control" ;}
.coll-pen-invalid .coll-pen-editor-controls a.coll-pen-button:hover { border-color: "error" ;}
.coll-pen-editor-controls a.coll-pen-button { border-left: 1px solid; flex: 1 1 1ch;}
.coll-pen-editor-controls a.coll-pen-button:first-child { border-left: 0; }
.coll-pen-editor-field textarea { font-size: 0.9em; border: none; background-color: "background"; }
.coll-pen-textarea-measure { font-size: 0.9em; width: 1ch ; }
.coll-pen-editor-field { background-color: "background"; border: 1px solid "control"; border-radius: 3px; box-shadow: 1px 1px 2px "shadow";}
.coll-pen-editor-field.coll-pen-invalid { color: "error"; border: 1px solid "error" ; }
.coll-pen-editor-field textarea.coll-pen-invalid { color: "error" ; }
.coll-pen-editor-frame { border-bottom: 1px solid; border-color: inherit; }
.coll-pen-unchanged .coll-pen-editor-controls { border-top: 1px solid "status" ;}
.coll-pen-unchanged .coll-pen-editor-controls a.coll-pen-button { border-color: "status" ;}
.coll-pen-unchanged .coll-pen-editor-controls a.coll-pen-button:hover { border-color: "status" ;}
.coll-pen-editor-field.coll-pen-unchanged { color: "status"; border: 1px solid "status" ; }

.coll-pen-editor-field.coll-pen-adder { border: none; box-shadow: none; }
.coll-pen-adder { opacity: 0.5; } /*margin-bottom: 3px; }*/
.coll-pen-adder:hover { opacity: 1; }
.coll-pen-adder:focus-within { opacity: 1; }
.coll-pen-adder .coll-pen-editor-controls a.coll-pen-button { border: none ; border-radius: 3px;}
.coll-pen-adder .coll-pen-editor-controls { border: none; }

.coll-pen-input-tag { font-size: 0.9em; color: "status"; float: left; position: absolute; padding-top: 2px; padding-left: 0.5ch;}

.coll-pen-field input:focus { outline: none; box-shadow: 0px 0px 2px "highlight"; }
a.coll-pen-button { text-decoration: none ; color: "control"; }
a.coll-pen-button { cursor: pointer; padding: 2px 5px; }
a.coll-pen-button:hover { cursor: pointer; padding: 2px 5px; color: "active"; }
a.coll-pen-button-prev { text-align: left; }
a.coll-pen-button-next { text-align: right; }

a.coll-pen-button.coll-pen-disabled { color: "disabled"; cursor: default; }
a.coll-pen-button.coll-pen-disabled:hover {  color: "disabled"; }

.coll-pen-noselect { user-select: none; -moz-user-select: none; -webkit-user-select: none; -ms-user-select: none; }

.coll-pen-success-floater { color: "active"; position: relative; bottom: 2em; width: 0; text-align: center; opacity: 0;
white-space: nowrap;
                -webkit-animation: 2s ease-out 0s 1 coll-pen-floater;
                animation: 2s ease-out 0s 1 coll-pen-floater; }

.coll-pen-success-floater-bar { color: "active"; position: relative; bottom: 2em; height: 0; text-align: center; opacity: 0;
                -webkit-animation: 2s ease-out 0s 1 coll-pen-floater;
                animation: 2s ease-out 0s 1 coll-pen-floater; }

@keyframes coll-pen-floater {
  0%  { bottom: 0px; opacity: 1;}
  100% { bottom: 2em; opacity: 0;}
}
@-webkit-keyframes coll-pen-floater {
  0%  { bottom: 0px; opacity: 1;}
  100% { bottom: 2em; opacity: 0;}
}


.coll-pen-spinner {box-sizing: border-box;
                 display: inline-block; width: .8em; height: .8em; border-radius: 25%; border: 1px solid "control"; margin: .1em;
                 -webkit-animation: coll-pen-spinner-reshape 2s infinite ease-in-out both;
                 animation: coll-pen-spinner-reshape 2s infinite ease-in-out both; }

@-webkit-keyframes coll-pen-spinner-reshape {
  0%  { border-radius: 20% ; -webkit-transform: rotate(0deg) }
  25% { border-radius: 35% ; -webkit-transform: rotate(90deg)}
  50% { border-radius: 50% ; -webkit-transform: rotate(180deg) }
  75% { border-radius: 35% ; -webkit-transform: rotate(270deg) }
 100% { border-radius: 20% ; -webkit-transform: rotate(360deg)}
}

@keyframes coll-pen-spinner-reshape {
  0%  { border-radius: 20% ; transform: rotate(  0deg) }
  25% { border-radius: 35% ; transform: rotate( 90deg) }
  50% { border-radius: 50% ; transform: rotate(180deg) }
  75% { border-radius: 35% ; transform: rotate(270deg) }
 100% { border-radius: 20% ; transform: rotate(360deg) }
}

"))

(defn to-encoded-css-uri [s]
  (str "data:text/css;charest=UTF-8," (js/encodeURIComponent s)))

(def default-palette :dark)

(def palettes
  {:dark {:background "#3e3e3e"
          :foreground "#dbdbcd"
          :control "#deae8e"
          :active "#7cb7ba"
          :disabled "#a0a0a0"
          :string "#cb9292"
          :keyword "#beeabe"
          :symbol "#92dfe2"
          :number "#cfbe8e"
          :other "#93bef2"
          :idx "#909090"
          :status  "#7f9e7f"
          :shadow "rgba(0, 0, 0, .5)"
          :highlight "rgba(255,255,255,0.7)"
          :error "#cc3941"
          :font "Ubuntu Mono, Courier New, Courier, monospace"
          }
   :light {:background "#ffffff"
           :foreground "#665555"
           :control "darkorange"
           :active "darkcyan"
           :disabled "#a0a0a0"
           :string "darkred"
           :keyword "darkgreen"
           :symbol "darkblue"
           :number "#222222"
           :other "purple"
           :idx "#909090"
           :status  "#7f9e7f"
           :shadow "rgba(0, 0, 0, .2)"
           :highlight "rgba(50,100,50,0.8)"
           :error "#bb0000"
           :font "Ubuntu Mono, Courier New, Courier, monospace"}})



(defn- -generate-link [palette]
  (let [default (palettes default-palette)
        supplied-palette (cond
                           (keyword? palette) (palettes palette)
                           (map? palette) palette
                           :else nil)
        css-palette (merge default supplied-palette)]
  [:link {:rel "stylesheet" :type "text/css" :href (to-encoded-css-uri (build-css-string css-palette))}]))

(def gen-css-link-el (memoize -generate-link))

(defn get-depth-colors [palette]
  (let [default (palettes default-palette)
        supplied-palette (cond
                           (keyword? palette) (palettes palette)
                           (map? palette) palette
                           :else nil)
        css-palette (merge default supplied-palette)]
    [(:foreground css-palette)
     (:active css-palette)
     (:other css-palette)
     (:status css-palette)
     (:symbol css-palette)
     (:control css-palette)
     (:keyword css-palette)]))
