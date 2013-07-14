(ns nuotl-acceptance.core
  (:require [clj-webdriver.taxi :as taxi]
            [midje.sweet :refer [facts =>]])
  (:import [twitter4j StatusUpdate Twitter TwitterFactory]
           [twitter4j.conf PropertyConfiguration]))

(defn connect []
  (let [twitter-config (PropertyConfiguration.
              (clojure.java.io/input-stream "/Users/John/Dropbox/nuotltestuser.properties"))]
    (. (TwitterFactory. twitter-config) (getInstance))))

(def twitter (connect))

(defn send-tweet [message]
  (. twitter (updateStatus (StatusUpdate. message))))

(defn get-most-recent-tweet-text []
  (.  (nth (. twitter (getMentionsTimeline)) 0) (getText)))

(defn check-for-message [message]
  (loop [interval 3000 attempts 5 counter 0]
    (let [most-recent (get-most-recent-tweet-text)]
      (if (= counter attempts)
        false
        (if (nil? (re-find (re-pattern message) most-recent))
          (do
            (println message)
            (println most-recent)
            (Thread/sleep interval)
            (recur interval attempts (inc counter)))
          true
          )))))


(facts
 (do (send-tweet "@nextuptester blah blah"))
 (poll-for-message "You are unauthorized to use this service.") => true
 )

;(send-tweet "@nextuptester blah balh balh")
;(get-most-recent-tweet-text)

;(taxi/set-driver! {:browser :firefox})

;(taxi/to "http://google.co.uk")
