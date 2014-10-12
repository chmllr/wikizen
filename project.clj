(defproject wikizen "0.1.0-SNAPSHOT"
  :description "Simple Markdown-based Wiki engine"
  :url "https://github.com/chmllr/wikizen"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [hiccups "0.3.0"]
                 [garden "1.2.1"]]

  :plugins [[lein-cljsbuild "1.0.4-SNAPSHOT"]
            [lein-simpleton "1.3.0"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "wikizen"
              :source-paths ["src"]
              :compiler {
                :output-to "wikizen.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
