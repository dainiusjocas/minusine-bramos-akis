(ns mba.archive
  (:require [jsonista.core :as json]
            [org.httpkit.client :as http])
  (:import (java.time LocalDate)))

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
