(ns itunes-media-import.itunes
  (:import (javax.script ScriptEngineManager))
  (:require [itunes-media-import.db-stuff :as db]
            [clojure.string :as string])
  (:use    [itunes-media-import.common])
)

(defn getScriptEngine []
  (.getEngineByName (ScriptEngineManager.) "AppleScript")
)

(def scriptEngineHolder (delay (getScriptEngine)))

(defn scriptEngine [] @scriptEngineHolder)

(defn success [permanent-id]
  {:result-value true, :permanent-id permanent-id})

(defn failure [msg]
  {:result-value false, :msg msg})

(defn try-to-add-media [full-path cmd]
  (try
    ;(println cmd)
    (success (.eval (scriptEngine) cmd))
    (catch Exception e (do
                         (println "Could not import " full-path ", Error: " (.getMessage e))
                         (failure (.getMessage e))))
  )
)

(defn add-itunes-media [file]
  (def full-path (.getPath file))
  (def cmd (str "set iTunesFile to POSIX file \"" full-path "\"\n"
                "tell application \"iTunes\"\n"
                "copy (add iTunesFile) to newMovie\n"
                "set pid to newMovie's persistent ID\n"
                "end tell")
  )

  (try-to-add-media full-path cmd)
)

(defn add-movie-file [file]
  (def full-path (.getPath file))
  (def media-name (remove-extension (.getName file)))
  (def genre (.getName (.getParentFile file)))

  (def cmd (str "set iTunesFile to POSIX file \"" full-path "\"\n"
    "tell application \"iTunes\"\n"
    "copy (add iTunesFile) to newMovie\n"
    "set (genre of newMovie) to \"" genre "\"\n"
    "set (name of newMovie) to \"" media-name "\"\n"
    "set (video kind of newMovie) to movie\n"
    "set pid to newMovie's persistent ID\n"
    "end tell")
  )

  (try-to-add-media full-path cmd)

)

(defn get-series-number [file]
  (let [sdir (.getParentFile file)]
    (if (.isDirectory sdir)
      (let [season (.getName sdir)]
        (if (.startsWith season "S")
          (try (java.lang.Integer/parseInt (subs season 1))
            (catch Exception e (do
              (println "Could not read seasonNumber for File " file)
              1)))
          1))
      1)))

(defn get-episode-number [file]
  (let [episode-full (.getName file)
        ep-prefix (string/trim (apply str (take-while #(not= \- %) episode-full)))
        ep-number (apply str (reverse (take-while #(java.lang.Character/isDigit %) (reverse ep-prefix))))]
    
    (if (> (.length ep-number) 0)
      (java.lang.Integer/parseInt ep-number)
      1)
    )
  )

(defn add-series-file [file]
  (def full-path (.getPath file))

  (def series-name (.getName (.getParentFile (.getParentFile file))))
  (def episode-name (let [episode-full (remove-extension (.getName file))
                          ep-name (apply str (reverse (take-while #(not= \- %) (reverse episode-full))))] ep-name))

  (def series-number (get-series-number file))
  (def episode-number (get-episode-number file))

  (def cmd (str "set iTunesFile to POSIX file \"" full-path "\"\n"
    "tell application \"iTunes\"\n"
    "copy (add iTunesFile) to newMovie\n"
    "set (video kind of newMovie) to TV show\n"
    "set ((season number) of newMovie) to (" series-number ")\n"
    "set (show of newMovie) to \"" series-name " " series-number "\"\n"
    (if (not-empty episode-name)
      (str "set (name of newMovie) to \"" episode-number " " episode-name "\"\n")
      (str "set (name of newMovie) to \"Episode " episode-number "\"\n"))
    "set ((episode ID) of (newMovie)) to \"S" series-number "E" episode-number "\"\n"
    "set ((episode number) of newMovie) to " episode-number "\n"
    "set ((sort show) of (newMovie)) to \"" series-name " S" series-number "E" episode-number "\"\n"
    "set pid to newMovie's persistent ID\n"
    "end tell"
  ))

  (try-to-add-media full-path cmd)
)


(defn itunes-media-add [file kind]
  (case kind
    "movie"  (do
               (add-movie-file file)
             )
    "serie"  (do
               (add-series-file file)
              )
    "itunes" (do
               (add-itunes-media file)
             )
  )


)