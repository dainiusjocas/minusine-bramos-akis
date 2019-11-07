(ns mba.core
  (:require [beagle.phrases :as beagle]
            [clojure.core.async :refer [chan pipeline to-chan <!! close!]]
            [jsonista.core :as json]
            [org.httpkit.client :as http])
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

(defn search-in-archive-records
  [highlighter-fn archive-records opts]
  (let [concurrency (or (:concurrency opts) 16)
        out (chan (or concurrency 16))
        xf (map (fn [archive-record]
                  (let [html  (fetch-snapshot
                                (:original archive-record)
                                (:timestamp archive-record))
                        text (.text (.body ^Document (Jsoup/parse html)))
                        hits (highlighter-fn text)]
                    (assoc archive-record
                      :html html
                      :text text
                      :hits hits))))]
    (pipeline concurrency out xf (to-chan archive-records))
    (let [output (doall (map (fn [_] (<!! out)) (range (count archive-records))))]
      (close! out)
      output)))

(defn search-in-pages [{:keys [dictionary search]}]
  (let [highlighter-fn (beagle/highlighter dictionary)]
    (search-in-archive-records highlighter-fn (query-archive-cdx search) {})))

(comment
  (map #(select-keys % [:original :hits])
       (search-in-pages
         {:dictionary [{:text "Dainius Jocas"}]
          :search     {:url "tokenmill.lt" :from "2016" :limit 50}})))
