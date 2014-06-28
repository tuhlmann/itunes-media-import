(ns leiningen.gen-config
  (:require [robert.hooke]))

(defn gen-config [project & args]
  (println "Generate configuration file")
)

(defn examine [x]
  (println x))

(defn activate []
  (robert.hooke/add-hook #'compile
                         #'examine))