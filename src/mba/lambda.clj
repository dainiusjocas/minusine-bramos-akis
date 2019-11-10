(ns mba.lambda
  (:gen-class
    :name mba.Api
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [mba.json :as json])
  (:import (java.io BufferedWriter InputStream)))

(defn decode-body [^InputStream is]
  (try
    (-> is (io/reader) (json/decode))
    (catch Exception e
      (log/errorf "Failed to decode the body with exception '%s'" e))))

(defn write-to-os [os body]
  (with-open [^BufferedWriter w (io/writer os)]
    (.write w ^String (json/encode body))))

(defn -handleRequest [_ is os _]
  (let [{body :body} (decode-body is)]
    (write-to-os
      os
      {:statusCode 200
       :body       (json/encode body)})))
