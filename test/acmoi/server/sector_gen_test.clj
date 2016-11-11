(ns acmoi.server.sector-gen-test
  (:require [taoensso.timbre :as log]
            [clojure.spec :as s]

            [acmoi.server.sector-gen :as sg]
            [acmoi.shared.spec :as ss]
            )
  )

;; Switches for debugging
(s/check-asserts false)
(s/check-asserts true)

