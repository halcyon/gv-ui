(ns app.system
  (:require
    [clojure.java.io :as io]
    [clojure.data.csv :as csv]
    [clojure.string :as str]
    [untangled.server.core :as core]
    [app.api :as api]
    [app.handler.oauth2 :as oauth2]
    [bidi.bidi :as bidi]
    [om.next.server :as om]
    [taoensso.timbre :as timbre]
    [com.stuartsierra.component :as c]
    [om.next.impl.parser :as op]))

(defn logging-mutate [env k params]
  (timbre/info "Mutation Request: " k)
  (api/apimutate env k params))

(defn logging-query [{:keys [ast] :as env} k params]
  (timbre/info "Query: " (op/ast->expr ast))
  (api/api-read env k params))

(let [current-table-id (atom -1)]
  (defn new-table-id!
    []
    (let [res @current-table-id]
      (swap! current-table-id inc)
      res)))

(defn seed-db
  [filenames]
  (reduce
   (fn [acc [tag fname]]
     (with-open [in-file (io/reader fname)]
       (let [table-id        (new-table-id!)
             [header & rows] (take 400 (csv/read-csv in-file))   ;; FIXME: rm `take`
             table-rows      (into []
                                   (map-indexed (fn [idx [path count]]
                                                  {:id [table-id idx]
                                                   :path (into [] (str/split path #"-"))
                                                   :count count}))
                                   rows)]
         (merge-with (fn [old new]
                       (throw (ex-info "duplicate table IDs"
                                       {:old old :new new})))
                     acc
                     {table-id {:id     table-id
                                :fname  fname
                                :tag    tag
                                :header {:id [table-id 0] :cols header}
                                :rows   table-rows}}))))
   {}
   filenames))


(defrecord Database [items next-id]
  c/Lifecycle
  (start [this] (assoc this
                  :items   (atom [])
                  :next-id (atom 1)
                  :tables  (atom (seed-db
                                  [[:srp "resources/public/data/srp.csv"]
                                   [:pdp "resources/public/data/pdp.csv"]]))))
  (stop [this] this))

(defn make-system
  []
  (core/make-untangled-server
    :config-path "config/demo.edn"
    :parser (om/parser {:read logging-query :mutate logging-mutate})
    :parser-injections #{:db}
    :components {:db (map->Database {})}
    :extra-routes {:routes   ["/" {"oauth2" {:get {"/auth"     :oauth2-auth
                                                   "/redirect" :oauth2-access
                                                   "/contacts" :contacts}}}]
                   :handlers {:oauth2-auth   oauth2/auth
                              :oauth2-access oauth2/redirect-handler
                              :contacts      oauth2/contacts}}))
