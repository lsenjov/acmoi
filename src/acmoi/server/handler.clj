(ns acmoi.server.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [taoensso.timbre :as log]
            [clojure.data.json :as json]
            )
  )

(defroutes
  app-routes
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
