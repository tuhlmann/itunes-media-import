(ns itunes-media-import.main
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.java.jdbc :as sql]
    [taoensso.timbre :as log]
    [itunes-media-import.common :refer :all]
    [itunes-media-import.db-stuff :as db]
    [itunes-media-import.itunes :as itunes])

  (:gen-class))

(defn load-config [filename]
 (edn/read-string (slurp filename)))

(defn media-files [dir]
  "Creates a lazy seq of all files matching the movie pattern in this and all sub dirs"
  (filter #(re-matches #"[a-zA-Z0-9].*\.m4v" (.getName %))
          (file-seq (io/file dir)))
)

(defn sync-media-dir [dir-config]
  (let [file-root (:dir dir-config)
        all (doall (media-files file-root))
        dir-id (:dir-id dir-config)]
    (log/info "sync " file-root)
    ;; Cleanup removed files from itunes and DB
    (doseq [db-entry (db/all-entries dir-id)]
      (let [full-path-from-db (relative-to-full file-root (:path db-entry))]
        (log/info "Check if file exists: " full-path-from-db)
        (if-not (find-first #(= full-path-from-db (.getPath %)) all)
          (do
            ; movie removed, remove from db
            (log/info "REMOVE: " full-path-from-db "with ID " (:perm_id db-entry))
            (itunes/remove-itunes-media full-path-from-db (:perm_id db-entry))
            (db/del-entry-by-id (:id db-entry))
        ))))
    ;; Add new files to itunes and DB
    (doseq [f all]
      (let [full-path (.getPath f)
            relative-path (full-to-relative  file-root full-path)]
        (if-not (db/contains-entry dir-id relative-path)
          (do
            ; entry not found add to itunes
            (def itunes-result (itunes/itunes-media-add f (:kind dir-config)))
            (if (:result-value itunes-result)
              (db/add-entry dir-id relative-path (:permanent-id itunes-result))
          )))))
  )
)

(defn -main [& args]
  (log/info "Synchronize media directories with iTunes")
  (let [config (load-config (str (app-home) "/config.edn"))]
    (when-not (db/initialized?)
      (db/init-db))
    (doseq [dir-config (:directories config)]
      (sync-media-dir dir-config)))
)

