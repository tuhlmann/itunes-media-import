(defproject itunes-media-import "0.1.0-SNAPSHOT"
  :description "Synchronize media directories with iTunes"
  :url "http://agynamix.de"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta2"]
                 [com.h2database/h2 "1.4.196"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [java-jdbc/dsl "0.1.3"]
                 [org.slf4j/slf4j-nop "1.7.25"]
                 [com.jolbox/bonecp "0.8.0.RELEASE"]
                 [com.taoensso/timbre "4.10.0"]              ; Logging
                 ]
  
  :plugins      [
                 [lein-tar "3.2.0"]
                 [lein-ancient "0.6.10"]
                 ]

  :tar {:uberjar true}
  :aot :all
  :main itunes-media-import.main)
