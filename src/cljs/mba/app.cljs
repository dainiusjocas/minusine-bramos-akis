(ns mba.app
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [mba.table :as table]
    [clojure.string :as s]
    [reagent.debug]
    [reagent.core :as r]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    ["@material-ui/core" :as mui]
    ["@material-ui/icons" :as mui-icons]
    [clojure.string :as s]))

(defn event-value
  [^js/Event e]
  (let [^js/HTMLInputElement el (.-target e)]
    (.-value el)))

(defonce text-state (r/atom ""))

(defonce found-data (r/atom [{:original "https://www.jocas.lt/", :hits [{:meta {}, :dict-entry-id "0", :begin-offset 881, :type "PHRASE", :end-offset 886, :text "Jocas"} {:meta {}, :dict-entry-id "0", :begin-offset 939, :end-offset 944, :type "PHRASE", :text "Jocas"} {:meta {}, :dict-entry-id "0", :end-offset 3316, :begin-offset 3311, :type "PHRASE", :text "Jocas"}], :archive-url "http://web.archive.org/web/20191109152029/https://www.jocas.lt/"}
                             {:original "https://www.jocas.lt/", :hits [{:meta {}, :dict-entry-id "0", :begin-offset 881, :type "PHRASE", :end-offset 886, :text "Jocas"} {:meta {}, :dict-entry-id "0", :begin-offset 939, :end-offset 944, :type "PHRASE", :text "Jocas"} {:meta {}, :dict-entry-id "0", :end-offset 3316, :begin-offset 3311, :type "PHRASE", :text "Jocas"}], :archive-url "http://web.archive.org/web/20191110084139/https://www.jocas.lt/"}]))

(defonce text-search-menu (r/atom {:open? false
                                   :case-sensitive? false}))

(def cell-style
  {:padding 2})

(defonce input-state (r/atom {"first-phrase" ""
                              "from-date"    "2019-11-01"}))

(defn call-mba
  ([] (call-mba {:dictionary [{:text "Jocas"}]
                 :search     {:url "jocas.lt"}}))
  ([params]
   
   (go
     (let [response (<! (http/post "https://t39kq6o310.execute-api.eu-central-1.amazonaws.com/Prod/observe"
                                   {:with-credentials? false
                                    :headers           {"Content-Type" "application/json"}
                                    :timeout           60000
                                    :json-params       params}))]
       (reset! found-data (:body response))))))

(defn execute-mba [e]
  (let [input @input-state
        dictionary (remove #(s/blank? (:text %))
                           [{:text (get input "first-phrase")}
                            {:text (get input "second-phrase")}
                            {:text (get input "third-phrase")}
                            {:text (get input "fourth-phrase")}])
        url (get input "first-domain")
        from (when-let [from-date (get input "from-date")]
               (if (not (s/blank? from-date))
                 (s/replace from-date "-" "")))
        to (when-let [date-to (get input "date-to")]
             (s/replace date-to "-" ""))]
    (if (and (seq dictionary) (not (s/blank? url)))
      (call-mba {:dictionary dictionary
                 :search     (cond-> {:url url}
                                     from (assoc :from from)
                                     to (assoc :from to))})
      (println "BAD INPUT"))))

(defn simple-component []
  [:div
   [:> mui/AppBar {:position "fixed"}
    [:> mui/Toolbar
     [:> mui/Button {:edge       "start"
                     :className  "menuButton"
                     :color      "inherit"
                     :aria-label "menu"
                     :style      {:marginRight 2}}
      [:> mui-icons/Menu]]
     [:> mui/Typography {:variant "h6" :className "title" :style {:flexGrow 1}} "Minusinė Bramos Akis"]
     #_[:> mui/Button {:className "helpButton" :color "inherit"} [:> mui-icons/Help]]]]
   [:> mui/Toolbar]

   [:br]

   [:div {:style {:flexGrow 1}}
    [:> mui/Grid {:container true :spacing 1 :direction "row"}
     [:> mui/Grid {:item true :xs 12 :sm 6}
      [:> mui/Paper {:style cell-style}
       [:> mui/TextField {:required true
                          :id (get @input-state "first-phrase")
                          :label "Frazė"
                          :margin "normal"
                          :on-change (fn [e] (swap! input-state assoc "first-phrase" (event-value e)))}]]]
     [:> mui/Grid {:item true :xs 12 :sm 6}
      [:> mui/Paper {:style cell-style}
       [:> mui/TextField {:id (get @input-state "second-phrase")
                          :label "Frazė"
                          :margin "normal"
                          :on-change (fn [e] (swap! input-state assoc "second-phrase" (event-value e)))}]]]
     [:> mui/Grid {:item true :xs 12 :sm 6}
      [:> mui/Paper {:style cell-style}
       [:> mui/TextField {:id (get @input-state "third-phrase")
                          :label "Frazė"
                          :margin "normal"
                          :on-change (fn [e] (swap! input-state assoc "third-phrase" (event-value e)))}]]]
     [:> mui/Grid {:item true :xs 12 :sm 6}
      [:> mui/Paper {:style cell-style}
       [:> mui/TextField {:id (get @input-state "fourth-phrase")
                          :label "Frazė"
                          :margin "normal"
                          :on-change (fn [e] (swap! input-state assoc "fourth-phrase" (event-value e)))}]]]
     [:> mui/Grid {:item true :xs 12 :sm 6}
      [:> mui/Paper {:style cell-style}
       [:> mui/TextField {:id        "date-from"
                          :label     "Nuo"
                          :type      "date"
                          :margin    "normal"
                          :value     (get @input-state "from-date")
                          :on-change (fn [e] (swap! input-state assoc "from-date" (event-value e)))
                          :className "from-date"}]]]
     [:> mui/Grid {:item true :xs 12 :sm 6}
      [:> mui/Paper {:style cell-style}
       [:> mui/TextField {:id        "date-to"
                          :label     "Iki"
                          :type      "date"
                          :margin    "normal"
                          :value     (get @input-state "date-to")
                          :on-change (fn [e] (swap! input-state assoc "date-to" (event-value e)))
                          :className "date-to"}]]]
     [:> mui/Grid {:item true :xs 12 :sm 6}
      [:> mui/Paper {:style cell-style}
       [:> mui/TextField {:required true
                          :id (get @input-state "first-domain")
                          :label "Domenas"
                          :defaultValue ""
                          :margin "normal"
                          :on-change (fn [e] (swap! input-state assoc "first-domain" (event-value e)))}]]]
     [:> mui/Grid {:item true :xs 12 :sm 6}
      [:> mui/Paper {:style cell-style} [:> mui/Button
                                         {:color :primary
                                          :variant :contained
                                          :on-click execute-mba}
                                         "Ieškoti" [:> mui-icons/Search]]]]]]

   [:div [:> mui/Divider {:className   "divider"
                          :orientation "horizontal"
                          :variant     "middle"
                          :absolute    true}]]

   [:br]

   [:div (table/mui-table found-data)]])

(defn init []
  (r/render [simple-component]
            (js/document.getElementById "root")))

(defn reload! [] (init))
