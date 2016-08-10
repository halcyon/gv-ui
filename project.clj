(defproject untangled/demo "1.0.0"
  :description "Demo"
  :url ""
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89"]
                 [org.omcljs/om "1.0.0-alpha40"]
                 [navis/untangled-client "0.5.4"
                  :exclusions [cljsjs/react
                               org.omcljs/om
                               yahoo.intl-messageformat-with-locales]]
                 [navis/untangled-server "0.6.1"]
                 [navis/untangled-spec "0.3.8" :exclusions [bidi prismatic/schema]]
                 [com.taoensso/timbre "4.6.0" :exclusions [io.aviso/pretty]]
                 [commons-codec "1.10"]
                 [binaryage/devtools "0.7.2" :scope "test"]
                 ;; [cljsjs/d3 "3.5.16-0"]
                 [cljsjs/d3 "3.5.7-1"]
                 [com.cemerick/url "0.1.1"]
                 [clj-http "3.1.0"]
                 [cheshire "5.6.3"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.csv "0.1.3"]]

  :plugins [[lein-cljsbuild "1.1.3"]]

  :source-paths ["src/server"]
  :test-paths ["test/client"]
  :jvm-opts ["-server" "-Xmx1024m" "-Xms512m" "-XX:-OmitStackTraceInFastThrow"]
  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :profiles {:dev {:source-paths ["dev/server"]
                   :dependencies [[figwheel-sidecar "0.5.4-7" :exclusions [commons-io]]
                                  [com.cemerick/piggieback "0.2.1"]]}
             :uberjar {:aot :all
                       :main app.main
                       :prep-tasks ["compile" ["cljsbuild" "once" "production"]]}
             :repl-options {:init-ns user :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src/client" "dev/client"]
                :figwheel     true
                :compiler     {:main                 cljs.user
                               :asset-path           "js/compiled/dev"
                               :output-to            "resources/public/js/compiled/app.js"
                               :output-dir           "resources/public/js/compiled/dev"
                               :optimizations        :none
                               :parallel-build       false
                               :verbose              false
                               :recompile-dependents true
                               :source-map-timestamp true}}

               {:id           "test"
                :source-paths ["test/client" "src/client"]
                :figwheel     true
                :compiler     {:main                 app.suite
                               :output-to            "resources/public/js/specs/specs.js"
                               :output-dir           "resources/public/js/compiled/specs"
                               :asset-path           "js/compiled/specs"
                               :recompile-dependents true
                               :optimizations        :none}}


               {:id           "production"
                :jar true
                :source-paths ["src/client"]
                :compiler     {:output-to       "resources/public/js/compiled/app.js"
                               :output-dir      "resources/public/js/compiled/production"
                               :pretty-print    false
                               :verbose         true
                               :closure-defines {goog.DEBUG false}
                               :elide-asserts   true
                               :optimizations   :advanced
                               :foreign-libs [{:provides ["cljsjs.d3"]
                                               :externs ["cljsjs/d3/common/d3.ext.js"]
                                               :file "cljsjs/d3/development/d3.inc.js"
                                               :file-min "cljsjs/d3/production/d3.min.inc.js"}]}}]}

  :repl-options {:init-ns user})
