(ns nuotl-acceptance.core
  (:require [clj-webdriver.taxi :as taxi]
            [midje.sweet :refer [facts fact =>]]
            [clj-yaml.core :as yaml]
            [clj-time.core :as t]
            [clj-time.local :as lt]
            [environ.core :refer [env]]
            [clojure.tools.logging :refer [debug]])
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

(defn get-most-recent-tweet []
  (let [status (nth (. twitter (getMentionsTimeline)) 0)]
    (println (. status (getCreatedAt)))
    status))

(defn text-contains-str? [tweet message]
  (do
    (println (. tweet (getText)))
    (println message)
    (not (nil? (re-find (re-pattern message) (. tweet (getText)))))))

(defn url-contains-str? [tweet string]
  (let [urls (. tweet (getURLEntities))]
    (if (> (count urls) 0)
      (let [url (. (nth urls 0) (getExpandedURL))]
        (println string)
        (println url)
        (not (nil? (re-find (re-pattern string) url))))
      false
      )))

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
         (Thread/sleep 1000)
         (let [response (get-most-recent-tweet)]
           (fact "Last tweet sent contains success message"
                 (text-contains-str? response "Your event tweet was successful!") => true)
           (fact "Last tweet has url with id of event tweet"
                 (url-contains-str? response (str (. tweet (getId)))) => true))
         (taxi/to   (format "%s/events/%s/%s" (config :frontend) year month))
         (fact (taxi/text (taxi/find-element {:css (format "#E%s .time" (. tweet (getId))) })) => (str hour ":" minutes))
         (fact (taxi/text (taxi/find-element {:css (format "#E%s .text" (. tweet (getId))) })) => (str "An event bbc.co.uk " random))
         (fact (taxi/text (taxi/find-element {:css (format "#E%s .name" (. tweet (getId))) })) => "Next Up Test User")
         (fact (taxi/text (taxi/find-element {:css (format "#E%s .area" (. tweet (getId))) })) => "North London")
         (. twitter (destroyStatus (. tweet (getId))))
         (debug "Made destroyStatus call - now sleeping for 1000ms")
         (Thread/sleep 1000)
         (taxi/refresh)
         (fact "Event no longer exists on page. " (taxi/exists? (format "#E%s" (. tweet (getId)))) => false)
         (let [response (get-most-recent-tweet)]
           (fact "Last tweet sent no longer has url with id of event tweet"
                 (url-contains-str? response (str (. tweet (getId)))) => false))
         ))))

(taxi/quit)
