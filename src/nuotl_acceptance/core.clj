(ns nuotl-acceptance.core
  (:require [clj-webdriver.taxi :as taxi]))

(defn account-name "In future should get this from twitter (based on credentials used)"
  []
  "@nuotl")

(defn site-location [] "http://preview.nextupontheleft.org/")

(def success-tweet
  (format "%s 2/3/2014 9pm 3h N An event is happening at www.somewhere.com" (account-name)))

(taxi/set-driver! {:browser :firefox} "http://preview.nextupontheleft.org/events/2013/3")
