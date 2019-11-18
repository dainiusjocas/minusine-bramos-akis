(ns mba.devcards
  (:require-macros
    [devcards.core :refer [defcard defcard-rg]])
  (:require
    [cljsjs.react]
    [cljsjs.react.dom]
    [reagent.core :as r :include-macros true]
    [devcards.core :as devcards]
    ["@material-ui/core" :as mui]
    [mba.table :as table]))

(def bmi-data (r/atom {:height 180 :weight 80}))

(defn calc-bmi [bmi-data]
  (let [{:keys [height weight bmi] :as data} bmi-data
        h (/ height 100)]
    (if (nil? bmi)
      (assoc data :bmi (/ weight (* h h)))
      (assoc data :weight (* bmi h h)))))

(defn slider [bmi-data param value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! bmi-data assoc param (.. e -target -value))
                        (when (not= param :bmi)
                          (swap! bmi-data assoc :bmi nil)))}])

(defn bmi-component [bmi-data]
  (let [{:keys [weight height bmi]} (calc-bmi @bmi-data)
        [color diagnose] (cond
                           (< bmi 18.5) ["orange" "underweight"]
                           (< bmi 25) ["inherit" "normal"]
                           (< bmi 30) ["orange" "overweight"]
                           :else ["red" "obese"])]
    [:div
     [:h3 "BMI XXXXXXX calculator"]
     [:div
      "Height: " (int height) "cm"
      [slider bmi-data :height height 100 220]]
     [:div
      "Weight: " (int weight) "kg"
      [slider bmi-data :weight weight 30 150]]
     [:div
      "BMI: " (int bmi) " "
      [:span {:style {:color color}} diagnose]
      [slider bmi-data :bmi bmi 10 50]]]))

#_(defcard bmi-calculator
           "*Code taken from the Reagent readme.*"
           (devcards/reagent bmi-component)
           bmi-data
           {:inspect-data true
            :frame true
            :history true})

(defn resp-component [resp]
  (let [ks (keys (first @resp))]
    [:table
     [:thead
      (into [:tr]
            (map (fn [k] ^{:key (str k)} [:th (name k)]) ks))]
     (into [:tbody]
           (map (fn [tr i] ^{:key (str i)}
                  (into [:tr]
                        (map (fn [k j]
                               ^{:key (str i j)} [:td (k tr)]) ks (range))))
                @resp (range)))]))

(defcard resp-table
         (devcards/reagent resp-component)
         [{:url "url1"
           :key "1"}
          {:url "url2"
           :key "2"}]
         {:inspect-data true
          :frame        true
          :history      true})

(defcard resp-table-mui
         (devcards/reagent table/mui-table)
         [{:url "url1"
           :key "1"}
          {:url "url2"
           :key "2"}]
         {:inspect-data true
          :frame        true
          :history      true})

(defcard resp-table-mui-2
         (devcards/reagent table/mui-table)
         [{:original "https://www.jocas.lt/", :hits [{:meta {}, :dict-entry-id "0", :begin-offset 881, :type "PHRASE", :end-offset 886, :text "Jocas"} {:meta {}, :dict-entry-id "0", :begin-offset 939, :end-offset 944, :type "PHRASE", :text "Jocas"} {:meta {}, :dict-entry-id "0", :end-offset 3316, :begin-offset 3311, :type "PHRASE", :text "Jocas"}], :archive-url "http://web.archive.org/web/20191109152029/https://www.jocas.lt/"} {:original "https://www.jocas.lt/", :hits [{:meta {}, :dict-entry-id "0", :begin-offset 881, :type "PHRASE", :end-offset 886, :text "Jocas"} {:meta {}, :dict-entry-id "0", :begin-offset 939, :end-offset 944, :type "PHRASE", :text "Jocas"} {:meta {}, :dict-entry-id "0", :end-offset 3316, :begin-offset 3311, :type "PHRASE", :text "Jocas"}], :archive-url "http://web.archive.org/web/20191110084139/https://www.jocas.lt/"}]
         {:inspect-data true
          :frame        true
          :history      true})

(defn ^:export main [] (devcards/start-devcard-ui!))

(defn reload! [] (main))
