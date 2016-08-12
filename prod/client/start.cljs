(ns app.start
  (:require
    [app.core :refer [app]]
    [app.ui :as ui]
    [untangled.client.core :as uc]))

(enable-console-print!)

;; (devtools/install! [:formatters :hints])

(js/console.log "Mounting app...")
(let [initialized-app (uc/mount @app ui/Root "app")]
  (js/console.log "Mounted app: " initialized-app)
  (js/console.log "resetting app atom...")
  (reset! app initialized-app)
  (js/console.log "app atom reset."))