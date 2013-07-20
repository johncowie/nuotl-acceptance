(ns nuotl-acceptance.core
  (:require [clj-webdriver.taxi :as taxi]
            [midje.sweet :refer [facts =>]]
            [clj-yaml.core :as yaml]
            [clj-time.core :as t]
            [clj-time.local :as lt]
            [environ.core :refer [env]])
  (:import [twitter4j StatusUpdate Twitter TwitterFactory]
           [twitter4j.conf PropertyConfiguration]))

(println (str "Using config file: " (env :config-file)))

(def config (yaml/parse-string (slurp (env :config-file))))

(defn connect []
  (let [twitter-config (PropertyConfiguration.(clojure.java.io/input-stream (config :twitter-user-properties)))]
    (. (TwitterFactory. twitter-config) (getInstance))))

(def twitter (connect))

(defn send-tweet [message]
  (println "Sending message: " message)
  (. twitter (updateStatus (StatusUpdate. message))))

(defn get-most-recent-tweet-text []
  (let [status (nth (. twitter (getMentionsTimeline)) 0)]
    (println (. status (getCreatedAt)))
    (. status (getText))))

(defn check-for-message [message]
  (loop [interval 2000 attempts 1 counter 0]
    (do
      (Thread/sleep interval)
      (let [most-recent (get-most-recent-tweet-text)]
        (if (= counter attempts)
          false
          (do
            (println most-recent)
            (println message)
            (if (nil? (re-find (re-pattern message) most-recent))
              (recur interval attempts (inc counter))
              true
              )))))))

(taxi/set-driver! {:browser (keyword (config :browser))})

(facts
 (let [tomorrow (t/plus (lt/local-now) (t/days 2))
       day     (t/day tomorrow)
       month   (t/month tomorrow)
       year    (t/year tomorrow)
       hour    15
       minutes 30
       random  (Math/random)
       ]
   (do (let [tweet (send-tweet (format "@%s %s %s 3h n An event bbc.co.uk %s"
                                       (config :twitter-listener)
                                       (str day "/" month "/" year)
                                       (str hour ":" minutes)
                                       random))]
         (check-for-message "Your event tweet was successful!") => true
         (taxi/to   (format "%s/events/%s/%s" (config :frontend) year month))
         (taxi/text (taxi/find-element {:css (format "#E%s .time" (. tweet (getId))) })) => (str hour ":" minutes)
         (taxi/text (taxi/find-element {:css (format "#E%s .text" (. tweet (getId))) })) => (str "An event bbc.co.uk " random)
         (taxi/text (taxi/find-element {:css (format "#E%s .name" (. tweet (getId))) })) => "Next Up Test User"
         (taxi/text (taxi/find-element {:css (format "#E%s .area" (. tweet (getId))) })) => "North London"
         (. twitter (destroyStatus (. tweet (getId))))
         ))))

(taxi/quit)
