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


(defresource changetag
  :available-media-types ["text/plain"]
  ;; etag changes every 10s
  :etag (let [i (int (mod (/ (System/currentTimeMillis) 10000) 10))]
          (.substring "abcdefhghijklmnopqrst"  i (+ i 10)))
  :handle-ok (format "It's now %s" (java.util.Date.)))

(def posts (ref []))

(defresource postbox
  :allowed-methods [:post :get]
  :available-media-types ["text/html"]
  :handle-ok (fn [ctx]
               (format (str "<html>Post text/plain to this resource.<br>\n"
                            "There are %d posts at the moment.</html>")
                       (count @posts)))
  :post! (fn [ctx]
           (dosync
             (let [body (slurp (get-in ctx [:request :body]))
                   id   (count (alter posts conj body))]
               {::id id})))
  ;; actually http requires absolute urls for redirect but let's
  ;; keep things simple.
  :post-redirect? (fn [ctx] {:location (format "/postbox/%s" (::id ctx))})
  :etag (fn [_] (str (count @posts))))

(defresource postbox-get [x]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :exists? (fn [ctx]
             (if-let [d (get @posts (dec (Integer/parseInt x)))]
               {::data d}))
  :handle-ok ::data)


(defroutes app
  (ANY "/secret" []
       secret)
  (ANY "/choice" []
       choice)
  (ANY "/babel" []
       babel)
  (ANY "/timehop" []
       timehop)
  (ANY "/changetag" []
       changetag)
  (ANY "/postbox" []
       postbox)
  (ANY "/postbox/:x" [x]
      (postbox-get x)))

(def handler
  (-> app
      wrap-params))


