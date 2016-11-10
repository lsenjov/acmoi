(ns acmoi.server.sector-gen
  (:require [taoensso.timbre :as log]
            [clojure.spec :as s]

            [acmoi.shared.spec :as ss]
            )
  )

(def ^:private filename
  "The file sectors are saved and loaded from"
  "sector.edn")

(defn- sector-load
  "Loads a sector from storage. Currently just loads from file"
  []
  {:post [(s/or :nil nil?
                :sector (s/assert ::ss/sector %))]}
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

(def default-sector
  "The default setup of an empty sector. Useful for testing."
  {:zone "ABC"
   ;; The next id to use
   :citizenId 1
   :citizens {}}
  )

(defonce ^:private sector
  (if-let [z (sector-load)]
    (agent z)
    (agent default-sector)
    )
  )

(defn- generate-first-name
  "Generates a first name"
  [gender]
  {:pre [(s/assert ::ss/gender gender)]
   :post [(s/assert ::ss/fName %)]}
  "TestName" ;; TODO
  )

(defn- generate-citizen
  "Generates a brand new adult citizen for the sector"
  [{:keys [citizenId zone] :as sector}]
  {:post [(s/assert ::ss/citizenMap %)]}
  (let [gender (rand-nth [:male :female])]
    {:gender gender
     :fName (generate-first-name gender)
     :clearance :IR
     :zone zone
     :citizenId citizenId
     :cloneNum 1
     }
    )
  )

(defn add-new-citizen
  "Adds a generated citizen to a sector, and increments the next id"
  [sector]
  {:pre [(s/assert ::ss/sector sector)]
   :post [(s/assert ::ss/sector %)]}
  (-> sector
      (assoc-in [:citizens (:citizenId sector)] (generate-citizen sector))
      (update-in [:citizenId] inc)
      )
  )
