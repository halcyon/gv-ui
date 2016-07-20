(ns app.system
  (:require
    [untangled.server.core :as core]
    [app.api :as api]
    [app.handler.demo :as demo]
    [app.handler.oauth2 :as oauth2]
    [om.next.server :as om]
    [taoensso.timbre :as timbre]
    [com.stuartsierra.component :as c]
    [om.next.impl.parser :as op]
    [ring.middleware.params :refer [wrap-params]]))

(defn logging-mutate [env k params]
  (timbre/info "Mutation Request: " k)
  (api/apimutate env k params))

(defn logging-query [{:keys [ast] :as env} k params]
  (timbre/info "Query: " (op/ast->expr ast))
  (api/api-read env k params))

(defrecord Database [items next-id]
  c/Lifecycle
  (start [this] (assoc this
                  :items (atom [])
                  :next-id (atom 1)))
  (stop [this] this))

(defn make-system
  []
  (core/make-untangled-server
    :config-path "config/demo.edn"
    :parser (om/parser {:read logging-query :mutate logging-mutate})
    :parser-injections #{:db}
    :components {:db (map->Database {})}
    :extra-routes {:routes   ["/" {"demo"   {:get :demo}
                                   "oauth2" {:get {"/auth"     :oauth2-auth
                                                   "/redirect" :oauth2-access
                                                   "/contacts" :contacts}}}]
                   :handlers {:demo          demo/handler
                              :oauth2-auth   oauth2/auth
                              :oauth2-access oauth2/redirect
                              :contacts      oauth2/contacts}}))
