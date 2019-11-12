#!/usr/bin/env bash

"exec" "clojure" "-Sdeps" "{:deps {org.clojure/tools.namespace {:mvn/version \"0.3.1\"}}}" "$0" "$@"

"USAGE: ./compile.clj"

(require
  '[clojure.edn :as edn]
  '[clojure.java.io :as io]
  '[clojure.string :as str]
  '[clojure.tools.namespace.find :as ns.find])

(prn "Starting to AOT compile sources")

(doall
  (->> (slurp "deps.edn")
       (edn/read-string)
       (:paths)
       (map io/file)
       (map ns.find/find-namespaces-in-dir)
       (flatten)
       (map compile)
       (str/join ", ")
       (printf "Compiled: [%s]\n")))

(prn "Finished AOT compilation")
