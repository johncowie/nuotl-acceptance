(defproject nuotl-acceptance "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-webdriver "0.6.0"]
                 [org.twitter4j/twitter4j-core "3.0.3"]
                 [midje "1.4.0"]
                 [clj-yaml "0.4.0"]
                 [clj-time "0.4.4"]
                 [environ "0.4.0"]
                 [org.clojure/tools.logging "0.2.6"]
                 [log4j/log4j "1.2.17"]
                 ]
  :resource-paths ["resources"]
  :plugins       [[lein-midje "2.0.1"]]
  )
