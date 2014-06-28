(ns itunes-media-import.common
  (:require [clojure.string :as string]))

(defn find-first [f coll]
  (first (filter f coll)))

(defn full-to-relative [file-root full-path]
  "Takes the full file path and subtracts a fixed root directory, returning a relative path"
  (let [rel (string/replace full-path file-root "")]
    (if (= (get rel 0) \/)
      (subs rel 1)
      rel))
)

(defn relative-to-full [file-root relative-path]
  "Takes the file root and a relative path returning a full path"
  (str file-root "/" relative-path))


(defn remove-extension [name]
  (def pos (.lastIndexOf name "."))
  (if (> pos -1)
    (subs name 0 pos)
    name
  ))