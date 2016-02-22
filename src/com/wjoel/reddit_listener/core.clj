(ns com.wjoel.reddit-listener.core
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(def user-agent "reddit-listener/0.1 by wjoel")
(def access-token-endpoint "https://www.reddit.com/api/v1/access_token")
(def read-scope "read")
(def millis-between-api-calls 2000)

(defn make-new-session [access-token client-id client-secret]
  {:access-token access-token
   :client-id client-id
   :client-secret client-secret
   :last-api-call-time (atom (System/currentTimeMillis))
   :millis-for-last-api-call (atom 200)})

(defn time-since-last-api-call [session]
  (- (System/currentTimeMillis)
     @(:last-api-call-time session)))

(defn oauth-get [session endpoint request-body request-params]
  (let [millis-since-last-call @(:last-api-call-time session)
        millis-to-sleep (- millis-since-last-call
                           (System/currentTimeMillis))]
    (when (> millis-to-sleep 0)
      (Thread/sleep millis-to-sleep)))

  (let [authorization (str "bearer " (:access-token session))
        api-call-start-time (System/currentTimeMillis)
        response (client/get endpoint
                             {:basic-auth [(:client-id session)
                                           (:client-secret session)]
                              :headers {"Authorization" authorization
                                        "User-Agent" user-agent}
                              :body request-body
                              :query-params request-params})
        millis-now (System/currentTimeMillis)]
    (reset! (:last-api-call-time session)
            millis-now)
    (reset! (:millis-for-last-api-call session)
            (- millis-now @(:millis-for-last-api-call session)))
    (some-> response
            :body
            (json/decode true)
            :data)))

(defn oauth-get-listing [session endpoint request-body request-params]
  (some->> (oauth-get session endpoint request-body request-params)
           :children
           (map :data)))

(defn get-session [client-id client-secret]
  (let [content-type "application/x-www-form-urlencoded"
        response (client/post access-token-endpoint
                              {:basic-auth [client-id client-secret]
                               :headers {"User-Agent" user-agent}
                               :content-type content-type
                               :body (str "grant_type=client_credentials")})
        access-token (some-> response
                             :body
                             (json/decode true)
                             :access_token)]
    (when access-token
      (make-new-session access-token client-id client-secret))))

(defn get-new-posts [session subreddit
                     {:keys [before after]}]
  (oauth-get session
             (str "https://www.reddit.com/r/" subreddit "/new.json")
             nil
             (merge {:limit 100}
                    (when (or before after)
                      {:before before
                       :after after}))))

(defn get-new-posts-seq
  ([session subreddit]
   (get-new-posts-seq session subreddit
                      (get-new-posts session subreddit {})))
  ([session subreddit {:keys [before after children] }]
   (cons (map :data children)
         (lazy-seq
          (get-new-posts-seq session subreddit
                             (get-new-posts session subreddit
                                            {:before before
                                             :after after}))))))

(defn api-timed-seq
  ([session s]
   (api-timed-seq session 0 nil s))
  ([session time-to-sleep pieces s]
   (if (empty? pieces)
     (let [pieces (first s)
           time-to-sleep (/ (max (- millis-between-api-calls
                                    (time-since-last-api-call session)
                                    @(:millis-for-last-api-call session))
                                 0)
                            (count pieces))]
       (cons (first pieces)
             (lazy-seq
              (api-timed-seq session time-to-sleep (rest pieces) (rest s)))))
     (do
       (Thread/sleep time-to-sleep)
       (cons (first pieces)
             (lazy-seq
              (api-timed-seq session time-to-sleep (rest pieces) s)))))))

(defn new-posts-stream [session subreddit]
  (api-timed-seq session (get-new-posts-seq session subreddit)))
