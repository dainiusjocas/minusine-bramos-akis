(ns mba.app
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.debug]
    [reagent.core :as r]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]))

(defn call-mba []
  (go
    (let [response (<! (http/post "https://t39kq6o310.execute-api.eu-central-1.amazonaws.com/Prod/observe"
                                  {:with-credentials? false
                                   :headers           {"Content-Type" "application/json"}
                                   :timeout           60000
                                   :json-params       {:dictionary [{:text "Jocas"}]
                                                       :search     {:url "jocas.lt"}}}))]
      (prn (:status response))
      (prn (js/Date.))
      (prn response))))

(defn simple-component []
  [:div
   [:p "I am a component!"]
   [:button
    {:onClick call-mba}
    "Buttonasdd"]
   [:p.someclass
    "I haves  " [:strong "bold"]
    [:span {:style {:color "red"}} " and red "] "text."]])

(defn init []
  (r/render [simple-component]
            (js/document.getElementById "root")))

(defn reload! []
  (println "Code updated.")
  (init))
