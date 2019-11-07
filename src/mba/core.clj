(ns mba.core
  (:require [beagle.phrases :as beagle]
            [clojure.core.async :refer [chan pipeline to-chan <!! close!]]
            [mba.archive :as archive])
  (:import (org.jsoup Jsoup)
           (org.jsoup.nodes Document)))

(defn search-in-archive-records
  [highlighter-fn archive-records opts]
  (let [concurrency (or (:concurrency opts) 16)
        out (chan (or concurrency 16))
        xf (map (fn [archive-record]
                  (let [html  (archive/fetch-snapshot
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
    (search-in-archive-records highlighter-fn (archive/query-archive-cdx search) {})))

(comment
  (map #(select-keys % [:original :hits])
       (search-in-pages
         {:dictionary [{:text "Dainius Jocas"}]
          :search     {:url "tokenmill.lt" :from "2016" :limit 50}})))
