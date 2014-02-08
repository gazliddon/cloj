(defproject cloj "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :url "http://example.com/FIXME"

            :dependencies [[org.clojure/clojure "1.5.1"]
                           [euclidean "0.2.0"]
                           [crate "0.2.5"]
                           [org.clojure/tools.namespace "0.2.4"]
                           [org.clojure/clojurescript "0.0-2156"]
                           [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                           [org.clojure/math.numeric-tower "0.0.4"]]

            :plugins [[lein-kibit "0.0.8"]
                      [lein-cljsbuild "1.0.0-alpha2"]]

            :source-paths ["src" ]

            :main gaz.play

            :cljsbuild {
                        :crossovers [gaz]
                        :crossover-path "crossovers"
                        :crossover-jar false 
                        :builds [
                                 {:id "dev"
                                  :source-paths ["src"]
                                  :compiler {
                                             :output-to "cloj.js"
                                             :output-dir "out"
                                             :pretty-print true
                                             :optimizations :none
                                             :source-map true}}
                                 {:id "opt"
                                  :source-paths ["src"]
                                  :compiler {
                                             :externs ["three.externs.js"]
                                             :output-to "cloj.js"
                                             :output-dir "out-opt"
                                             :optimizations :advanced
                                             :source-map "test.js.map"}}]})


