(ns mba.core
  (:require [org.httpkit.client :as http]
            [jsonista.core :as json]
            [beagle.phrases :as beagle])
  (:import (org.jsoup Jsoup)
           (org.jsoup.nodes Document)
           (java.time LocalDate)))

(defrecord archive-record [url-key timestamp original mimetype status-code digest length])

(defn fetch-snapshot [original timestamp]
  @(http/request
     {:method :get
      :url    (format "http://web.archive.org/web/%s/%s" timestamp original)}
     (fn [resp] (-> resp :body))))

(defn query-archive-cdx
  "Returns a list of archive-records"
  [{:keys [url limit from to]}]
  @(http/request
     {:method :get
      :url    (format "http://web.archive.org/cdx/search/cdx?url=%s&output=json&limit=%s&from=%s&to=%s"
                      url
                      (or limit 5)
                      (or from "1990")
                      (or to (inc (.getYear (LocalDate/now)))))}
     (fn [resp]
       (let [[_ & data] (-> resp :body (json/read-value))]
         (map (fn [data-line] (apply ->archive-record data-line)) data)))))

; (search-in-pages
;  {:dictionary [{:text "Dainius Jocas"}]
;   :search {:url "tokenmill.lt" :from "2016" :limit 50}})

(defn search-in-pages [{:keys [dictionary search]}]
  (let [highlighter (beagle/highlighter dictionary)]
    (->> (query-archive-cdx search)
         (map (fn [archive-item]
                (assoc archive-item
                  :html (fetch-snapshot
                          (:original archive-item)
                          (:timestamp archive-item)))))
         (map (fn [with-html]
                (assoc with-html :text (.text (.body ^Document (Jsoup/parse (:html with-html)))))))
         (map (fn [with-text]
                (assoc with-text :highlights (highlighter (:text with-text))))))))
