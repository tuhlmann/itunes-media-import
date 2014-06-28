(defproject itunes-media-import "0.1.0-SNAPSHOT"
  :description "Synchronize media directories with iTunes"
  :url "http://agynamix.de"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.h2database/h2 "1.4.178"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [java-jdbc/dsl "0.1.0"]
                 [org.slf4j/slf4j-nop "1.7.7"]                 
                 [com.jolbox/bonecp "0.8.0.RELEASE"]]
  :plugins      [[lein-tar "3.2.0"]]
  :tar {:uberjar true}
  ;:hooks [gen-config]
  :aot :all
  :main itunes-media-import.main)
