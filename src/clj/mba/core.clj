(ns mba.core
  (:require [beagle.phrases :as beagle]
            [clojure.core.async :refer [chan pipeline to-chan <!! close!]]
            [mba.archive :as archive]
            [clojure.tools.logging :as log])
  (:import (org.jsoup Jsoup)
           (org.jsoup.nodes Document)))

(defn search-in-archive-records
  [highlighter-fn archive-records opts]
  (let [concurrency (or (:concurrency opts) 64)
        out (chan (or concurrency 64))
        xf (map (fn [archive-record]
                  (let [archive-url (archive/wayback-machine-url archive-record)
                        html  (archive/get-body archive-url)
                        text (.text (.body ^Document (Jsoup/parse html)))
                        hits (highlighter-fn text)]
                    (assoc archive-record
                      :archive-url archive-url
                      :html html
                      :text text
                      :hits hits))))]
    (pipeline concurrency out xf (to-chan archive-records))
    (let [output (doall (map (fn [_] (<!! out)) (range (count archive-records))))]
      (close! out)
      output)))

(defn search-in-pages [{:keys [dictionary search]}]
  (let [highlighter-fn (beagle/highlighter dictionary)]
    (search-in-archive-records highlighter-fn
                               (log/spy  (archive/fetch-coordinates
                                  (merge search
                                         {:filter {::archive/statuscode 200
                                                   ::archive/mimetype   "text/html"}})
                                  {:n 50})) {})))

(comment
  (map #(select-keys % [:original :hits :archive-url])
       (remove #(empty? (:hits %)) (search-in-pages
                                     {:dictionary [{:text "Karbauskis"}]
                                      :search     {:url "delfi.lt/news/.*" :from "2016"}}))))
