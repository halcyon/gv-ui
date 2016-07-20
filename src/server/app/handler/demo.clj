(ns app.handler.demo
  (:require
   [taoensso.timbre :as timbre]
   [ring.util.response :as ring]))

(defn exec-demo
  "Vanilla D3 sequence visualization"
  [{req :request :as env} match]
  (-> (ring/response "send more money")
      (ring/content-type "text/plain; charset=UTF-8")))

(defn handler
  "docstring"
  [env match]
  (let [{:keys [db request]} env]
    (timbre/info "env: " env)
    (timbre/info "match: " match)
    (-> (ring/response "demo world!")
        (ring/content-type "text/plain; charset=UTF-8"))))
