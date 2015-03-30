(defproject clojure-tetris "1.0.0-SNAPSHOT"
  :description "Tetris coded with Clojure"
  :url "https://github.com/tatsy/clojure-tetris"
  :license "Eclipse Public License - v 1.0"
  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [deflayout "0.9.0-SNAPSHOT"]]

  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}}

  :repl-options {:timeout 5000}

  :aot [tetris.Block]
  :source-paths ["src"]
  :test-paths ["test"]
  :compile-path "classes"

  :main "tetris.core")
