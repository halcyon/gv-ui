(ns app.start
  (:require
    [app.core :refer [app]]
    [app.ui :as ui]
    [devtools.core :as devtools]
    [untangled.client.core :as uc]))

(enable-console-print!)

(devtools/install! [:custom-formatters :sanity-hints])

(js/console.log "Mounting app...")
(let [initialized-app (uc/mount @app ui/Root "app")]
  (js/console.log "Mounted: " initialized-app)
  (reset! app initialized-app))
