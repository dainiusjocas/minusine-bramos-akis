(ns mba.table
  (:require ["@material-ui/core" :as mui]))

(def selectors {:phrase   {:header-title "Paieškos frazė"
                           :selector     #(-> % :phrase)
                           :preview      #(-> % str)}
                :snippet  {:header-title "Ištrauka"
                           :selector     #(-> % :snippet)
                           :preview      #(-> % str)}
                :original {:header-title "Dabar"
                           :selector     #(-> % :original)
                           :preview      (fn [val] [:a {:href val
                                                        :target "_blank"} "Atidaryti"])}
                :archive  {:header-title "Praeityje"
                           :selector     #(-> % :archive-url)
                           :preview      (fn [val] [:a {:href val
                                                        :target "_blank"} "Archyvas"])}
                :observed-date {:header-title "Data praeityje"
                                 :selector     #(-> % :archive-url)
                                 :preview      (fn [val] [:a {:href val
                                                              :target "_blank"} (last (re-find #"http://web.archive.org/web/(\d+)/" val))])}})

(defn prepare-data [resp]
  (mapcat (fn [{:keys [original hits archive-url]}]
            (map (fn [{:keys [text]}]
                   {:original original
                    :archive-url archive-url
                    :phrase text}) hits)) resp))

(defn mui-table [resp]
  (let [data (prepare-data @resp)
        ks [:phrase :snippet :original :observed-date]]
    [:> mui/Paper {:className "root"}
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