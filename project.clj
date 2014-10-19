(defproject wikizen "0.1.0-SNAPSHOT"
            :description "Simple Markdown-based Wiki engine"
            :url "https://github.com/chmllr/wikizen"

            :dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/clojurescript "0.0-2311"]
                           [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

            :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
                      [lein-simpleton "1.3.0"]
                      [com.cemerick/clojurescript.test "0.3.1"]]

            :profiles {:dev {:plugins [[com.cemerick/austin "0.1.5"]]}}

            :source-paths ["src"]

            :cljsbuild {:builds [{:id "dev"
                                  :source-paths ["src"]
                                  :compiler {:output-to "wikizen.js"
                                             :externs ["lib/marked.min.js" "lib/diff_match_patch.js"]
                                             :output-dir "out"
                                             :optimizations :none
                                             :source-map true}}
                                 {:id "test"
                                  :source-paths ["src" "test"]
                                  :compiler {:output-to "wikizen.js"
                                             :output-dir "out/test"
                                             :optimizations :simple}}
                                 {:id "prod"
                                  :source-paths ["src"]
                                  :compiler {:output-to "wikizen.js"
                                             :externs ["lib/marked.min.js" "lib/diff_match_patch.js"]
                                             :optimizations :simple}}]
                        :test-commands
                                {"phantom" ["phantomjs" :runner
                                            "testMode = true;"
                                            "lib/diff_match_patch.js"
                                            "wikizen.js"]}})
