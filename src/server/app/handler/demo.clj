(ns app.handler.demo
  (:require
   [taoensso.timbre :as timbre]
   [ring.util.request :as ring-req]
   [ring.util.response :as ring]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.resource :as rr :refer [wrap-resource]]))



(defn exec-demo
  "Vanilla D3 sequence visualization"
  [req]
  (timbre/info :resource (rr/resource-request req "public" {:loader nil}))
  (timbre/info :path-info (ring-req/path-info req))
  (timbre/info :req req)
  (-> (ring/response "Oops... something went wrong!")
      (ring/status 500)
      (ring/content-type "text/plain; charset=UTF-8")))

(defn handler
  [env match]
  (let [hdlr (-> exec-demo
                 (wrap-resource "public")
                 (wrap-content-type)
                 (wrap-not-modified))]
    (hdlr (:request env))))

(defn resource-handler
  [req]

  (-> (ring/response "ahh! not-found")
      (ring/content-type "text/plain; charset=UTF-8")))

(defn resource
  [env match]
  (let [hdlr (-> resource-handler
                 (wrap-resource "public")
                 (wrap-content-type)
                 (wrap-not-modified))]
    (hdlr (:request env))))
