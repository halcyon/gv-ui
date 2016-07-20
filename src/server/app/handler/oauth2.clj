(ns app.handler.oauth2
  (:require [cemerick.url :refer [query->map url-encode]]
            [cheshire.core :as json]
            [ring.util.response :as ring]
            [taoensso.timbre :as timbre]
            [clj-http.client :as http]
            [clojure.data.xml :as xml]))

(def oauth2-params
  {:client-id (System/getenv "GOOGLE_CLIENT_ID")
   :client-secret (System/getenv "GOOGLE_CLIENT_SECRET")
   :authorize-uri "https://accounts.google.com/o/oauth2/auth"
   :redirect-uri "http://127.0.0.1:8080/oauth2/redirect"
   :access-token-uri "https://accounts.google.com/o/oauth2/token"
   :scope "profile https://www.googleapis.com/auth/contacts"})

(def auth-db (atom {}))

(defn auth
  [env match]
  (let [{:keys [db request]} env
        csrf (str (gensym "csrf-"))
        uri (str (:authorize-uri oauth2-params)
                 "?response_type=code"
                 "&client_id=" (url-encode (:client-id oauth2-params))
                 "&redirect_uri=" (url-encode (:redirect-uri oauth2-params))
                 "&scope=" (url-encode (:scope oauth2-params))
                 "&state=" (url-encode csrf))]

    (timbre/info "env: " env)
    (timbre/info "match: " match)

    (prn csrf)
    (swap! auth-db assoc :csrf csrf)
    (ring/redirect uri)))


(defn request-grant
  [authorization-code]
  (try
    (-> oauth2-params
        :access-token-uri
        (http/post
         {:form-params {:code         authorization-code
                        :grant_type   "authorization_code"
                        :client_id    (:client-id oauth2-params)
                        :redirect_uri (:redirect-uri oauth2-params)}
          :basic-auth  [(:client-id oauth2-params) (:client-secret oauth2-params)]})
        :body
        (json/parse-string true))
    (catch Exception _ nil)))

(defn redirect
  [env match]
  (let [{:keys [db request]} env
        query-string (clojure.walk/keywordize-keys (query->map (:query-string request)))
        csrf (:state query-string)
        code (:code query-string)]
    (timbre/info "env:" env)
    (timbre/info "match:" match)
    (timbre/info "query-string:" query-string)
    (timbre/info "csrf-atom:" (:csrf @auth-db))

    (if (= csrf (:csrf @auth-db))
      (do
        (swap! auth-db merge (request-grant code))
        (ring/content-type (ring/response (:access_token @auth-db)) "text/html"))
      (ring/content-type (ring/response (str "CSRF attempt detected "
                                             "token: " csrf " "
                                             "atom:  " (:csrf @auth-db)))
                         "text/html"))))

(defn contacts
  [env match]
  (let [contacts-resp (http/get "https://www.google.com/m8/feeds/contacts/default/full"
                                {:headers
                                 {:authorization (str "Bearer " (:access_token @auth-db))}})
        contacts-xml (xml/parse-str (:body contacts-resp))]
    (ring/content-type (ring/response contacts-xml) "text/html")))
