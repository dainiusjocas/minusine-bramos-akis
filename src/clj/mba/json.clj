(ns mba.json
  (:require [jsonista.core :as json]))

(def keys-mapper (json/object-mapper {:decode-key-fn true}))

(defn decode [input]
  (json/read-value input keys-mapper))

(defn encode [input]
  (json/write-value-as-string input))
