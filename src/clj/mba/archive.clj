(ns mba.archive
  (:require [clojure.tools.logging :as log]
            [clojure.spec.alpha :as s]
            [jsonista.core :as json]
            [org.httpkit.client :as http]))

; API for https://github.com/internetarchive/wayback/tree/master/wayback-cdx-server#basic-usage

(defn save [url]
  (http/request
    {:method  :get
     :timeout 5000
     :url     (str "https://web.archive.org/save/" url)}
    (fn [{:keys [error] :as resp}]
      (if error
        (log/warnf "Error archiving `%s`" error)
        (log/debugf "Archived `%s`" resp)))))

(defn get-body [url]
  @(http/request
     {:method :get
      :url    url}
     (fn [resp] (-> resp :body))))

(defn wayback-machine-url [{:keys [original timestamp]}]
  (format "http://web.archive.org/web/%s/%s" timestamp original))

(defn fetch-snapshot [coordinate]
  (get-body (wayback-machine-url coordinate)))

(defn remove-nil-vals [m] (into {} (remove (comp nil? second) m)))

(s/def ::urlkey (s/nilable string?))
(s/def ::timestamp (s/nilable string?))
(s/def ::original (s/nilable string?))
(s/def ::mimetype (s/nilable string?))
(s/def ::statuscode (s/nilable (or pos-int? string?)))
(s/def ::digest (s/nilable string?))
(s/def ::length (s/nilable string?))
(s/def ::dupecount (s/nilable string?))

(s/def ::strings (s/coll-of string?))

(s/def ::filter
  (s/keys :req-un []
          :opt-un [::urlkey ::timestamp ::original ::mimetype
                   ::statuscode ::digest ::length ::dupecount]))

(def default-query-params
  {:output        "json"
   :limit         5
   :offset        0
   :showDupeCount true
   :fastLatest    true
   :collapse      "digest"})

(defn prepare-filters [filters]
  (map #(str (name (first %)) ":" (second %)) (remove-nil-vals filters)))

(s/fdef prepare-filters
        :args (s/cat :filter ::filter)
        :ret ::strings)

(defn prepare-query-params [default-query-params query-params]
  (remove-nil-vals
    (merge default-query-params
           (update query-params :filter prepare-filters))))

(defn query-archive-cdx
  "Returns a list of archive-records"
  [query-params]
  (log/debugf "Querying CDX with %s" query-params)
  @(http/request
     {:method       :get
      :url          "http://web.archive.org/cdx/search/cdx"
      :query-params (prepare-query-params default-query-params query-params)}
     (fn [{:keys [error body status] :as resp}]
       (log/spy resp)
       (when error
         (throw (RuntimeException. (str error))))
       (when (= 200 status)
         (let [[header & data] (json/read-value body)
               keys (map keyword header)]
           (map (fn [data-line] (zipmap keys data-line)) data))))))

(def default-step 1000)

(defn fetch-coordinates
  "Returns a lazy sequence of coordinates. Total amount of coordinates fetched can be defined
   by :n in opts. If :n is not provided then *all* coordinates will be fetched lazily.
   Query :limit is interpreted as a max chunk size in the iteration.
   If query :limit is not provided then default-step is used."
  ([query] (fetch-coordinates query {}))
  ([{:keys [offset limit] :as query} {:keys [n previous-was-empty? fetched] :as opts}]
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
