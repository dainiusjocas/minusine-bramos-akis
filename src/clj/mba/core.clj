(ns mba.core
  (:require [beagle.phrases :as beagle]
            [clojure.core.async :refer [chan pipeline to-chan <!! close!]]
            [clojure.string :as string]
            [mba.archive :as archive])
  (:import (org.jsoup Jsoup)
           (org.jsoup.nodes Document)))

(defn extract-snippet [text highlight]
  (format "<...>%s<b>%s</b>%s<...>"
          (subs text (max 0 (- (:begin-offset highlight) 30)) (:begin-offset highlight))
          (:text highlight)
          (subs text (:end-offset highlight) (min (count text) (+ (:end-offset highlight) 30)))))

(defn search-in-archive-records
  [highlighter-fn archive-records opts]
  (let [archive-records (remove nil? archive-records)
        concurrency (or (:concurrency opts) 64)
        out (chan (or concurrency 64))
        xf (map (fn [archive-record]
                  (let [archive-url (archive/wayback-machine-url archive-record)
                        html  (archive/get-body archive-url)]
                    (when-not (string/blank? html)
                      (let [text (.text (.body ^Document (Jsoup/parse html)))
                            hits (map #(assoc % :snippet (extract-snippet text %)) (highlighter-fn text))]
                        (assoc archive-record
                          :archive-url archive-url
                          :html html
                          :text text
                          :hits hits))))))]
    (pipeline concurrency out xf (to-chan archive-records))
    (let [output (doall (map (fn [_] (<!! out)) (range (count archive-records))))]
      (close! out)
      (remove nil? output))))

(defn search-in-pages [{:keys [dictionary search]}]
  (let [highlighter-fn (beagle/highlighter dictionary)]
    (search-in-archive-records highlighter-fn
                               (archive/fetch-coordinates
                                 (merge search
                                        {:filter {::archive/statuscode 200
                                                  ::archive/mimetype   "text/html"}})
                                 {:n 50}) {})))

(comment
  (map #(select-keys % [:original :hits :archive-url])
       (remove #(empty? (:hits %)) (search-in-pages
                                     {:dictionary [{:text "Karbauskis"}]
                                      :search     {:url "delfi.lt/news/.*" :from "2016"}}))))
