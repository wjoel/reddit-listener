(defproject com.wjoel/reddit-listener "0.0.1-SNAPSHOT"
  :description "reddit streams"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [clj-http "2.1.0"]
                 [cheshire "5.5.0"]]
  :repl-options {:init-ns reddit-listener.core})
