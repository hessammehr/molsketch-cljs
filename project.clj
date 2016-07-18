(defproject molsketch-cljs "0.1.0-SNAPSHOT"
  :description "A 2D molecule sketcher in ClojureScript for publication quality chemical structures."
  :url "https://github.com/hessammehr/molsketch-cljs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha10"]
                 [org.clojure/clojurescript "1.9.89"]
                 [org.clojure/core.async "0.2.385"]
                 [reagent "0.6.0-rc"]
                 [com.rpl/specter "0.11.2"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-figwheel "0.5.4-7"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                :figwheel {:on-jsload "molsketch-cljs.core/on-js-reload"}

                :compiler {:main molsketch-cljs.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/molsketch-cljs.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/molsketch-cljs.js"
                           :main molsketch-cljs.core
                           :optimizations :advanced
                           :pretty-print false}}
               {:id "test"
                 :source-paths ["src" "test/cljs"]
                 :figwheel {:on-jsload "molsketch-cljs.core-test/on-js-reload"}

                 :compiler {:main molsketch-cljs.core-test
                            :asset-path "js/compiled/test/out"
                            :output-to "resources/public/js/compiled/test/test.js"
                            :output-dir "resources/public/js/compiled/test/out"
                            :source-map true
                            ; :cache-analysis true
                            :optimizations :none
                            :source-map-timestamp true}}]}



  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"
             :css-dirs ["resources/public/css"]}) ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ; :nrepl-port 7888})

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
