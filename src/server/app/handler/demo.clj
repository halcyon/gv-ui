(ns app.handler.demo
  (:require
   [taoensso.timbre :as timbre]
   [ring.util.response :as ring]))

(defn handler
  "Vanilla D3 sequence visualization"
  [env match]
  (let [{:keys [db request]} env]
    (timbre/info "env: " env)
    (timbre/info "match: " match)
    (-> (ring/response "send more money")
        (ring/content-type "text/plain; charset=UTF-8"))))
