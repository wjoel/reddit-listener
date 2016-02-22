(ns com.wjoel.reddit-listener.core-test
  (:require [clojure.java.io :as io])
  (:use [clojure.test]
        [com.wjoel.reddit-listener.core]))

(def credentials (-> (io/resource "test-credentials.clj")
                     slurp
                     read-string))

(def session (get-session (:client-id credentials)
                          (:client-secret credentials)))

(deftest get-oauth-token
  (testing "Access token should be a string if get-session succeeded")
  (is (instance? String (:access-token session))))
