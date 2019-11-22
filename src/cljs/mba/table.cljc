(ns mba.table
  (:require ["@material-ui/core" :as mui]))

(def selectors {:phrase        {:header-title "Paieškos frazė"
                                :selector     #(-> % :phrase)
                                :preview      #(-> % str)}
                :snippet       {:header-title "Ištrauka"
                                :selector     #(-> % :snippet)
                                :preview      #(-> % str)}
                :original      {:header-title "Dabar"
                                :selector     #(-> % :original)
                                :preview      (fn [val] [:a {:href   val
                                                             :target "_blank"} "Atidaryti"])}
                :observed-date {:header-title "Data praeityje"
                                :selector     #(select-keys % [:archive-url :observed-date])
                                :preview      (fn [{:keys [archive-url observed-date]}]
                                                [:a {:href   archive-url
                                                     :target "_blank"} observed-date])}})

(defn prepare-data [resp]
  (mapcat (fn [{:keys [original hits archive-url observed-date]}]
            (map (fn [{:keys [snippet text]}]
                   {:original      original
                    :snippet       snippet
                    :archive-url   archive-url
                    :observed-date observed-date
                    :phrase        text}) hits)) resp))

(defn mui-table [resp]
  (let [data (prepare-data @resp)
        ks [:phrase :snippet :original :observed-date]]
    [:> mui/Paper {:className "root" :style {:display (if (seq data) "inline" "none")}}
     [:> mui/Table {:className "table" :aria-label "simple-table"}
      [:> mui/TableHead {:style {:backgroundColor "grey"}}
       (into [:> mui/TableRow]
             (map (fn [k] [:> mui/TableCell (-> selectors k :header-title)]) ks))]
      (into [:> mui/TableBody]
            (map (fn [r i]
                   (into [:> mui/TableRow {:hover true :key (str i)}]
                         (map (fn [k j]
                                ^{:key (str i j)}
                                [:> mui/TableCell
                                 ((or (-> selectors k :preview) str) ((-> selectors k :selector) r))]) ks (range))))
                 data (range)))]]))