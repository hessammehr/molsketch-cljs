(defproject molsketch-cljs "0.1.0-SNAPSHOT"
  :description "A 2D molecule sketcher in ClojureScript for publication quality chemical structures."
  :url "https://github.com/hessammehr/molsketch-cljs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [com.rpl/specter "1.1.3"]]
                 

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :resource-paths ["resources" "target"]

  :profiles {:dev {:dependencies [[com.bhauman/figwheel-main "0.2.3"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]]}}

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "molsketch_cljs" "-r"]})
