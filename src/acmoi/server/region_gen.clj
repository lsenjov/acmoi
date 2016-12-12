(ns acmoi.server.region-gen
  "Details world generation"
  (:require [taoensso.timbre :as log]
            [clojure.spec :as s]

            [acmoi.shared.spec :as ss]
            [acmoi.shared.economy :as es]

            [acmoi.shared.helpers :as helpers]

            [acmoi.server.db :as db]
            )
  )

(def ^:private filenames
  "The file to read to get the rest of the filenames to load data from.
  The file should contain a single map, with the keys :goods, :reactions"
  "files.edn")

(defn- assoc-keywords
  "Given a map of keywords to objects, assocs the key under a :keyword index in value.
  Returns the map"
  [m]
  (apply merge
         {}
         (for [[k v] m]
           {k (assoc v :keyword k)}
           )
         )
  )

(defn reset-and-gen-new-world
  "WARNING: WILL ERASE ALL DATA
  Resets the database, and creates a new universe to simulate from config files.
  If it can't find the config files, won't reset anything.
  Returns truthy on success, else nil" ;; TODO return goods/reaction map on success?
  []
  (try (let [files (clojure.edn/read-string (slurp filenames))
             goods (->> files :goods slurp clojure.edn/read-string assoc-keywords)
             reactions (->> files :reactions slurp clojure.edn/read-string assoc-keywords)
             ;levels (->> files :levels slurp clojure.edn/read-string)
             ]
         (doall (for [[_ v] goods] (db/upsert-good v)))
         (doall (for [[_ v] reactions] (db/upsert-reaction v)))
         )
       (catch Exception e (do (log/error "Could not complete generation. Error:" e) nil))
       )
  )
