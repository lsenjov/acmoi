(ns acmoi.server.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [taoensso.timbre :as log]
            [clojure.data.json :as json]

            ;; Local data
            [acmoi.server.game :as acg]
            )
  )

(log/set-level! :trace)
(log/info "Logging initialised at info level")
(log/trace "Logging initialised at trace level")

(defroutes app-routes
  (GET "/api/citizen/basic/:userKey/:citizenId/" {{:keys [userKey citizenId] :as params} :params baseURL :context} (json/write-str (acg/get-citizen-basic userKey citizenId)))
  (GET "/api/citizen/basic/" {{:keys [userKey citizenId] :as params} :params baseURL :context} (json/write-str (acg/get-citizen-basic userKey citizenId)))
  (route/resources "/")
  (route/not-found
    (json/write-str {:status "error" :message "Invalid endpoint"}))
  )

(def app
  (-> app-routes
      (wrap-defaults api-defaults)
      ring.middleware.session/wrap-session
      )
  )
