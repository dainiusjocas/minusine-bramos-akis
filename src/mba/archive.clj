(ns mba.archive
  (:require [jsonista.core :as json]
            [org.httpkit.client :as http]
            [clojure.tools.logging :as log]))

(defrecord archive-record
  [url-key timestamp original mimetype status-code digest length dupecount])

(defn fetch-snapshot [original timestamp]
  @(http/request
     {:method :get
      :url    (format "http://web.archive.org/web/%s/%s" timestamp original)}
     (fn [resp] (-> resp :body))))

(defn remove-nil-vals [m] (into {} (remove (comp nil? second) m)))

(defn query-archive-cdx
  "Returns a list of archive-records"
  [{:keys [url limit from to offset]}]
  (log/debugf "Limit %s offset %s" limit offset)
  @(http/request
     {:method       :get
      :url          "http://web.archive.org/cdx/search/cdx"
      :query-params (remove-nil-vals
                      {:url           url
                       :output        "json"
                       :from          from
                       :to            to
                       :limit         (or limit 5)
                       :offset        (or offset 0)
                       :showDupeCount true
                       :fastLatest    true
                       :collapse      "digest"})}
     (fn [resp]
       (let [[_ & data] (-> resp :body (json/read-value))]
         (map (fn [data-line] (apply ->archive-record data-line)) data)))))

(def default-step 1000)

(defn fetch-coordinates
  "Returns a lazy sequence of coordinates. Total amount of coordinates fetched can be defined
   by :n in opts. If :n is not provided then *all* coordinates will be fetched lazily.
   Query :limit is interpreted as a max chunk size in the iteration.
   If query :limit is not provided then default-step is used."
  ([query] (fetch-coordinates query {}))
  ([{:keys [offset limit] :as query} {:keys [n previous-was-empty? fetched] :as opts}]
   (log/debugf "Fetching %s" opts)
   (when (and (not previous-was-empty?) (or (nil? n) (nil? fetched) (< fetched n)))
     (let [step (if n
                  (min (or limit default-step) (- n (or fetched 0)))
                  (or limit default-step))
           chunk (query-archive-cdx (assoc query :limit step :offset offset))]
       (lazy-cat chunk (fetch-coordinates
                         (update query :offset (fnil + 0) step)
                         (-> opts
                             (assoc :previous-was-empty? (empty? chunk))
                             (update :fetched (fnil + 0) (count chunk)))))))))
