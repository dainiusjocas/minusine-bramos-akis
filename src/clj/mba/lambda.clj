(ns mba.lambda
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [mba.json :as json]
            [mba.core :as mba])
  (:import (java.io BufferedWriter InputStream))
  (:gen-class
    :name mba.Api
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]))

(defn decode-body [^InputStream is]
  (try
    (-> is (io/reader) (json/decode))
    (catch Exception e
      (log/errorf "Failed to decode the body with exception '%s'" e))))

(defn write-to-os [os body]
  (with-open [^BufferedWriter w (io/writer os)]
    (.write w ^String (json/encode body))))

(defn -handleRequest [_ is os _]
  (let [{body :body} (decode-body is)
        request (json/decode body)
        _ (log/infof "Received body: %s of type %s" request (type request))
        observed-content (->> request
                              (mba/search-in-pages)
                              (remove #(empty? (:hits %)))
                              (map #(select-keys % [:original :hits :archive-url :observed-date])))]
    (write-to-os
      os
      {:statusCode 200
       :headers    {"Access-Control-Allow-Origin" "*"
                    "Content-Type" "application/json"}
       :body       (json/encode observed-content)})))
