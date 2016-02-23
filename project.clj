(defproject com.wjoel/reddit-listener "0.0.1"
  :description "reddit streams"
  :url "https://github.com/wjoel/reddit-listener"
  :license {:name "BSD 3-clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [clj-http "2.1.0"]
                 [cheshire "5.5.0"]]
  :repl-options {:init-ns reddit-listener.core})
