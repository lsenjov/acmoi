(ns acmoi.server.db
  "This namespace is purely for saving and loading from the database.
  Each game is loaded into a separate namespace, including the raw creation paramaters (goods, reactions etc).
  Only allows for the running of a single game at a time."
  (:require [taoensso.timbre :as log]
            [clojure.spec :as s]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.util :as mu]
            )
  )

(def ^:private conn
  "The connection to the local database"
  (mg/connect)
  )

(def ^:private db-name
  "Name of the database"
  "acmoi"
  )
(def ^:private db
  "Actual database object"
  (mg/get-db conn db-name)
  )

;; The database uses a couple of collections.
;; c-inid for inids
;; c-reg for regions (of all sizes)
;; c-goods for goods
;; c-reactions for reactions
(def ^:private c-inid "Collection for inids" "inid")
(def ^:private c-reg "Collection for regions" "regions")
(def ^:private c-goods "Collection for goods" "goods")
(def ^:private c-reactions "Collection for reactions" "reactions")

(defn get-new-id
  "Returns a unique mongo id"
  []
  (str (mu/object-id))
  )

(defn reset-database
  "WARNING Drops and recreates the entire database"
  []
  (log/warn "Resetting database!")
  (mg/drop-db conn db-name)

  ;; Create inids
  (mc/create db c-inid {})

  ;; Create regions
  (mc/create db c-reg {})
  ;; TODO Add keys

  ;; Create goods list
  (mc/create db c-goods {})

  ;; Create reactions list
  (mc/create db c-reactions {})
  ;; TODO create keys

  )

(defn- upsert-item
  "Takes a collection and an item. Checks for _id, and if exists, upserts.
  If _id doens't exists, creates it.
  Returns the (possibly updated) item."
  [coll {_id :_id :as obj}]
  (if _id
    ;; Has id
    (do
      (mc/upsert db coll {:_id _id} obj)
      obj
      )
    ;; No id
    (upsert-item coll (assoc obj :_id (get-new-id)))
    )
  )

(defn upsert-inid
  "Creates or updates an inid in the database.
  Returns the (possibly updated) inid"
  [inid]
  (upsert-item c-inid inid)
  )
(defn get-inid-by-id
  "Gets an inid by id"
  [inidId]
  (mc/find-map-by-id db c-inid inidId)
  )

(defn upsert-region
  "Creates or updates a region in the database.
  Returns the (possibly updated) region."
  [region]
  (upsert-item c-reg region)
  )
(defn get-region-by-id
  "Gets a region by id"
  [regionId]
  (mc/find-map-by-id db c-reg regionId)
  )
(defn get-universe
  "Gets the universe region"
  []
  (mc/find-one-as-map db c-reg {:level 0}))

(defn upsert-good
  "Creates or updates a good in the database.
  Returns the (possibly updated) good"
  [good]
  (upsert-item c-goods good)
  )
(defn get-good-by-id
  "Gets a good by its id"
  [goodId]
  (mc/find-map-by-id db c-goods goodId)
  )

(defn upsert-reaction
  "Creates or updates a reaction in the database.
  Returns the (possibly updated) reaction"
  [reaction]
  (upsert-item c-reactions reaction)
  )
(defn get-reaction-by-id
  "Gets a reaction by its id"
  [reactionId]
  (mc/find-map-by-id db c-reactions reactionId)
  )

