(ns mba.app
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.debug]
    [reagent.core :as r]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    ["@material-ui/core" :as mui]
    ["@material-ui/icons" :as mui-icons]))

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
   [:h1 "Minusinė Bramos Akis"]
   [:br]
   [:> mui/FormControl
    [:> mui/InputLabel {:htmlFor "my-input"} "Kur ieškoti "]
    [:> mui/Input {:id "my-input" :aria-describedby "my-helper-text"}]
    [:> mui/FormHelperText {:id "my-helper-text"} "Ieškoti su Minusine Bramos Akimi"]
    [:> mui/Button
     {:color :primary
      :variant :contained
      :on-click #(println "moo")}
     "Ieškoti" [:> mui-icons/Search]]]])

(defn init []
  (r/render [simple-component]
            (js/document.getElementById "root")))

(defn reload! []
  (println "Code updated.")
  (init))
