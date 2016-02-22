# reddit-listener

Gets a stream of events from reddit.

## Usage

Get a session with

    $ (def session (get-session "your-reddit-client-id"
                                "your-reddit-client-secret"))

and use it to get a lazy stream of new posts

    $ (doseq [{:keys [title]} (take 200 (new-posts-stream session "all"))]
        (println title))

## Tests

To run tests, put your reddit credentials in `resources/test-credentials.clj`
followed by `lein test`
