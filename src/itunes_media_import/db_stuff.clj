(ns itunes-media-import.db-stuff
  (:require [clojure.java.io :as io]
           [clojure.java.jdbc :as jdbc]
           [java-jdbc.ddl :as ddl]
           [java-jdbc.sql :as sql])
  (:use    [itunes-media-import.common])
  (:import com.jolbox.bonecp.BoneCPDataSource)
)

(def db-store (str (app-home) "/syncstore.db"))

(def db-spec {:classname "org.h2.Driver"
              :subprotocol "h2"
              :subname db-store
              :user "sa"
              :password ""
              :naming {:keys clojure.string/upper-case
                       :fields clojure.string/upper-case}
              :init-pool-size 4
              :max-pool-size 20
              :partitions 1
              :idle-time (* 3 60 60)
              :MV_STORE false
              :MVCC false
  })

(defn pooled-datasource [db-spec]
  (let [{:keys [classname subprotocol subname user password
                init-pool-size max-pool-size idle-time partitions]} db-spec
    cpds (doto (BoneCPDataSource.)
      (.setDriverClass classname)
      (.setJdbcUrl (str "jdbc:" subprotocol ":" subname ";MV_STORE=false;MVCC=false"))
      (.setUsername user)
      (.setPassword password)
      (.setMinConnectionsPerPartition (inc (int (/ init-pool-size partitions))))
      (.setMaxConnectionsPerPartition (inc (int (/ max-pool-size partitions))))
      (.setPartitionCount partitions)
      (.setStatisticsEnabled true)
      (.setIdleMaxAgeInMinutes (or idle-time 60)))]
    {:datasource cpds}))
      
(def pooled-db-spec (pooled-datasource db-spec))

(defn init-db []
    (jdbc/db-do-commands pooled-db-spec false
      (ddl/create-table :entry
             [:id "integer PRIMARY KEY AUTO_INCREMENT"]
             [:dir_id "varchar(20)"]
             [:path "varchar(255)"]
             [:kind "varchar(20)"]
             [:perm_id "varchar(64)"]))
)

(defn add-entry [dir-id relative-path perm-id]
  (jdbc/insert! pooled-db-spec :entry
         {
         :dir_id dir-id
         :path relative-path
         :perm_id perm-id
         })
  (println "NEW: " relative-path)
)

(defn del-entry-by-id [id]
  (jdbc/delete! pooled-db-spec :entry (sql/where {:id id}))
)

(defn get-entry [dir-id relative-path]
  (jdbc/query pooled-db-spec
    (sql/select * :entry (sql/where {:dir_id dir-id, :path relative-path})))
)

(defn contains-entry [dir-id relative-path]
  (not (empty? (get-entry dir-id relative-path)))
)

(defn all-entries [dir-id]
  (jdbc/query pooled-db-spec
    (sql/select * :entry (sql/where {:dir_id dir-id})))
)

(defn initialized?
  "checks to see if the database schema is present"
  []
  (.exists (new java.io.File (str db-store ".h2.db")))
)
