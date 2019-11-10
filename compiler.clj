(require
  '[clojure.java.io :as io]
  '[clojure.string :as str]
  '[clojure.tools.namespace.find :as ns.find])

(prn "Starting to AOT compile sources")

(doall
  (->> ["classes" "src"]
       (map io/file)
       (map ns.find/find-namespaces-in-dir)
       flatten
       (map compile)
       (str/join ", ")
       (printf "Compiled: [%s]\n")))

(prn "Finished compilation")
