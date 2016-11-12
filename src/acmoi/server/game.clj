(ns acmoi.server.game
  (:require [taoensso.timbre :as log]
            [clojure.spec :as s]

            [acmoi.shared.spec :as ss]
            [acmoi.server.sector-gen :as ssg]

            [acmoi.shared.helpers :as helpers]
            )
  )

(def ^:private filename
  "The file gameSector is saved and loaded from"
  "gameSector.edn")

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

;(defonce ^:private gameSector
(def ^:private gameSector
  (if-let [z (sector-load)]
    (agent z)
    (agent (ssg/generate-sector 1000))
    )
  )

(defn get-citizen-basic
  "Gets the basic information for a citizen"
  [^String userKey ^String citizenId]
  (log/trace "get-citizen-basic. userKey:" userKey "citizenId:" citizenId)
  (if-let [c (get-in @gameSector [:citizens (helpers/parse-int citizenId)])]
    (helpers/filter-keys c #{:citizenId :clearance :fName :zone :cloneNum :gender :commendations :treason :dead?})
    )
  )
(defn get-citizen-associates
  "Gets the associates of a citizen. If none, returns empty vector. If no citizen, returns nil"
  [^String userKey ^String citizenId]
  (log/trace "get-citizen-associates. userKey:" userKey "citizenId:" citizenId)
  (if-let [c (get-in @gameSector [:citizens (helpers/parse-int citizenId)])]
    (if-let [a (get c :associates)]
      a
      []
      )
    nil
    )
  )

(defn- create-player
  "Gets an unused RED and associates it with a player"
  [sector uuid]
  (let [pId (->>
              ;; Get all red citizens
              (helpers/get-citizens-by-clearance @gameSector :R)
              ;; Just their citizen numbers
              keys
              (remove (->> sector :players vals (map :citizenId) set))
              rand-nth
              )
        ]
    (assoc-in sector [:players uuid] {:userKey uuid :citizenId pId})
    )
  )

(defn new-player
  "Gets an unused RED and associates it with a player. Returns the uuid in a map"
  []
  (log/trace "new-player")
  (let [uuid (helpers/uuid)]
    ;; Send it off to create the player
    (send-off gameSector create-player uuid)
    ;; Tell the player to wait on this key
    {:userKey uuid}
    )
  )
(defn log-in
  "Returns the user map of a uuid"
  [uuid]
  (log/trace "log-in")
  (if-let [p (get-in @gameSector [:players uuid])]
    p
    {}
    )
  )
