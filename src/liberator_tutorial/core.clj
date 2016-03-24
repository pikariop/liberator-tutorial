(ns liberator-tutorial.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY]]))


(defresource secret
  :available-media-types ["text/html"]
  :exists? (fn [ctx]
    (= "tiger"
       (get-in ctx [:request :params "word"])))
  :handle-ok "You found the secret word!"
  :handle-not-found "Uh, that's the wrong word. Guess again!")

(defresource choice
  :available-media-types ["text/html"]
  :exists? (fn [ctx]
             (if-let [choice
                      (get {"1" "stone" "2" "paper" "3" "scissors"}
                           (get-in ctx [:request :params "choice"]))]
               {:choice choice}))
  :handle-ok (fn [ctx]
               (format "<html>Your choice: &quot;%s&quot;.</html>"
                       (get ctx :choice)))
  :handle-not-found (fn [ctx]
                     (format "<html>There is no value for the option &quot;%s&quot;"
                             (get-in ctx [:request :params "choice"] ""))))

(defresource babel
  :available-media-types ["text/plain" "text/html"
                          "application/json" "application/clojure;q=0.9"]
  :handle-ok
    #(let [media-type
       (get-in % [:representation :media-type])]
       (condp = media-type
         "text/plain" "You requested plain text"
         "text/html" "<html><h1>You requested HTML</h1></html>"
    {:message "You requested a media type"
     :media-type media-type}))
  :handle-not-acceptable "Uh, Oh, I cannot speak those languages!")


(defresource timehop
  :available-media-types ["text/plain"]
  ;; timestamp changes every 10s
  :last-modified (* 10000 (long  (/ (System/currentTimeMillis) 10000)))
  :handle-ok (fn [_] (format "It's now %s" (java.util.Date.))))

(defroutes app
  (ANY "/secret" []
       secret)
  (ANY "/choice" []
       choice)
  (ANY "/babel" []
       babel)
  (ANY "/timehop" []
       timehop))

(def handler
  (-> app
      wrap-params))


