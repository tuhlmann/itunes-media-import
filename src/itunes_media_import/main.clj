(ns itunes-media-import.main
  (:require [clojure.edn :as edn]
           [clojure.java.io :as io]
           [clojure.java.jdbc :as sql]
           [itunes-media-import.db-stuff :as db]
           [itunes-media-import.itunes :as itunes])
  (:use    [itunes-media-import.common])
  (:gen-class))

(defn load-config [filename]
 (edn/read-string (slurp filename)))

(defn media-files [dir]
  "Creates a lazy seq of all files matching the movie pattern in this and all sub dirs"
  (filter #(re-matches #"[a-zA-Z0-9].*\.m4v" (.getName %))
          (file-seq (io/file dir)))
)

(defn sync-media-dir [dir-config]
  (println "sync " dir-config)
  (def file-root (:dir dir-config))
  (def all (doall (media-files file-root)))
  (def dir-id (:dir-id dir-config))
  (doseq [f all]
    (def full-path (.getPath f))
    (def relative-path (full-to-relative  file-root full-path))
    (println dir-id "/" relative-path)
    (if-not (db/contains-entry dir-id relative-path)
      (do
        ; entry not found add to itunes
        (println "not in db " relative-path)
        (def itunes-result (itunes/itunes-media-add f (:kind dir-config)))
        (if (:result-value itunes-result)
          (db/add-entry dir-id relative-path (:permanent-id itunes-result))
        )
      )
    )
  )
  (doseq [db-entry (db/all-entries dir-id)]
    (def full-path-from-db (relative-to-full file-root (:path db-entry)))
    (if-not (find-first #(= full-path-from-db (.getPath %)) all)
      (
        ; movie removed, remove from db
        (println "Not found, remove from db: " full-path-from-db)
        (db/del-entry-by-id (:id db-entry))
      )
    )
  )
)

(defn -main [& args]
  (println "In Main")
  (def config (load-config (str (app-home) "/config.edn")))
  (if-not (db/initialized?) (db/init-db))
  (doseq [dir-config (:directories config)]
    (sync-media-dir dir-config))
)

