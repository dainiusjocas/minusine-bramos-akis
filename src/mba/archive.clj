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
  [{:keys [url limit from to offset]}]
  @(http/request
     {:method       :get
      :url          "http://web.archive.org/cdx/search/cdx"
      :query-params {:url    url
                     :output "json"
                     :from   (or from "1990")
                     :to     (or to (inc (.getYear (LocalDate/now))))
                     :limit  (or limit 5)
                     :offset (or offset 0)}}
     (fn [resp]
       (let [[_ & data] (-> resp :body (json/read-value))]
         (map (fn [data-line] (apply ->archive-record data-line)) data)))))

(defn fetch-n-pages
  ([query n] (fetch-n-pages query n (min 1 n) 0))
  ([query n step offset]
   (prn "Step:" step "offset:" offset)
   (when (< offset n)
     (take n (lazy-cat (query-archive-cdx (assoc query :limit step :offset offset))
                       (fetch-n-pages query n step (+ step offset)))))))
