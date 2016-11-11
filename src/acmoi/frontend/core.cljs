(ns acmoi.frontend.core
  (:require [taoensso.timbre :as log]
            [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST] :as ajax]
            [cljs.js :as cjs]
            ))

(enable-console-print!)

(println "This text is printed from src/acmoi/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

;; TODO change back to defonce when required
(def app-state (atom
                 {:citizens {}
                  }
                 )
  )
(def userInfo (atom {:userCitizen 1 :userKey "tempApiKey"}))

(def ^:private colour-styles
  "Maps clearances to foreground and background colours"
  {:IR {:color "White" :background-color "Black"}
   :R {:color "White" :background-color "DarkRed"}
   }
  )

(defn- get-citizen-basic
  "Gets basic citizen info"
  [citizenId]
  (GET "/api/citizen/basic/"
       {:params {:citizenId citizenId
                 :userKey (:userKey @userInfo)}
        :response-format (ajax/json-response-format {:keywords? true})
        :handler (fn [m]
                   (swap! app-state assoc-in [:citizens citizenId] m)
                   (log/info "app-state is now:" @app-state)
                   )
        }
       )
  )

(defn- get-citizen-associates
  "Gets the associates of a citizen"
  [citizenId]
  (GET "/api/citizen/associates/"
       {:params {:citizenId citizenId
                 :userKey (:userKey @userInfo)}
        :response-format (ajax/json-response-format {:keywords? true})
        :handler #(do (log/trace "Requesting associates of citizen" citizenId)
                      (swap! app-state assoc-in [:citizens citizenId :associates] %)
                      )
        }
       )
  )

(defn- print-person-name
  "Prints a citizen's name from a map"
  [{:keys [fName zone clearance cloneNum]}]
  (if fName
    (str fName "-"
         (if (= :IR clearance)
           ""
           (str (name clearance) "-")
           )
         zone "-"
         cloneNum
         )
    "Unknown"
    )
  )

(defn create-citizen-box
  [n]
  (let [expand (atom false)]
    (fn []
      (let [cmap (get-in @app-state [:citizens n])]
        [:div {:style (-> {:margin "2px"}
                          (#(if @expand
                              (merge % {:border "1px solid white"})
                              %))
                          (merge (get colour-styles (keyword (:clearance cmap)) {:color "Red" :background-color "Black"}))
                          )
               :onClick #(if (not (:citizenId cmap)) (get-citizen-basic n) nil)
               }
         (if @expand
           [:span
            [:span {:onClick #(do (js/console.log (str "Clicked on person:" n))
                                  (swap! expand not)
                                  false)
                    }
             "Name: " (print-person-name cmap)] [:br]
            "Id Number:" n [:br]
            (if (:associates cmap)
              ;; Associates known
              [:span "Known associates:" [:br]
               (for [p (:associates cmap)]
                 [create-citizen-box p])
               ]
              ;; Associates unknown
              [:span {:onClick #(get-citizen-associates n)}
               "##Get Associates##"
               ]
              )

              ]
           [:span {:onClick #(do (js/console.log (str "Clicked on person:" n))
                                 (swap! expand not)
                                 false)
                   }
            (print-person-name cmap)]
           )
         ]
        )
      )
    )
  )
(defn hello-world []
  (let [expand (atom false)]
    (fn []
      [:div
       [:div (doall (for
                      ;;[n (-> @app-state :citizens keys)]
                      [n (range 1 6)]
                      ^{:key n}
                      [create-citizen-box n]
                                     ))]
       [:div {:style {:color "White"}}
        ]
       [:div {:style {:color "White"}}
        (-> (assoc (ajax/json-response-format) :keywords? true)
            pr-str
            )
        ]
       ]
      )
    )
  )

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
