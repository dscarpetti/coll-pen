(defproject coll-pen "0.2.1"
  :description "Reactive ClojureScript Collection Interactive Visualization"
  :url "https://dscarpetti.github.io/coll-pen/"
  :license {:copywrite "2020 David Scarpetti"
            :name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.773"]
                 [reagent "1.0.0-alpha2"]]

  :source-paths ["src"]

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" "coll-pen.test-runner"]}

  :plugins [[lein-cljsbuild "1.1.8"]]

  :cljsbuild {:builds [{:id "demo"
                        :source-paths ["src"]
                        :compiler {:output-to "docs/demo.js"
                                   :optimizations :advanced
                                   :output-dir "target/js/browser"
                                   :main coll-pen.demo.main
                                   :pretty-print false}}]}

  :profiles {:dev {:dependencies [[com.bhauman/figwheel-main "0.2.11"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]]

                   :resource-paths ["target"]
                   ;; need to add the compiled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["target"]}})
