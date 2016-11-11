(ns acmoi.server.game
  (:require [taoensso.timbre :as log]
            [clojure.spec :as s]

            [acmoi.shared.spec :as ss]
            [acmoi.server.sector-gen :as ssg]

            [acmoi.shared.helpers :as helpers]
            )
  )

(def ^:private filename
  "The file sectors are saved and loaded from"
  "sector.edn")

(defn- sector-load
  "Loads a sector from storage. Currently just loads from file"
  []
  {:post [(s/or :nil nil?
                :sector #(s/assert ::ss/sector %))]}
  (try
    (-> filename slurp clojure.edn/read-string)
    (catch java.io.FileNotFoundException e
      (do (log/info "Could not find file" filename "to load from.")
          nil
          )
      )
    )
  )
(defn- sector-save
  "Saves a sector to storage. Currently just saves to file
  Returns true on success, else false"
  [sector]
  {:pre [(s/assert ::ss/sector sector)]
   :post [(s/assert boolean? %)]}
  (try (do (spit filename (pr-str sector))
           true)
       (catch Exception e
         (do (log/error "Could not save to file" filename "Exception:" e)
             false)
         )
       )
  )

(defonce ^:private sector
  (if-let [z (sector-load)]
    (agent z)
    (agent (ssg/generate-sector 1000))
    )
  )

(defn get-citizen-basic
  "Gets the basic information for a citizen"
  [^String userKey ^String citizenId]
  (log/trace "get-citizen-basic. userKey:" userKey "citizenId:" citizenId)
  (if-let [c (get-in @sector [:citizens (helpers/parse-int citizenId)])]
    (helpers/filter-keys c #{:citizenId :clearance :fName :zone :cloneNum :gender :commendations :treason :dead?})
    )
  )
(defn get-citizen-associates
  "Gets the associates of a citizen. If none, returns empty vector. If no citizen, returns nil"
  [^String userKey ^String citizenId]
  (log/trace "get-citizen-associates. userKey:" userKey "citizenId:" citizenId)
  (if-let [c (get-in @sector [:citizens (helpers/parse-int citizenId)])]
    (if-let [a (get c :associates)]
      a
      []
      )
    nil
    )
  )
