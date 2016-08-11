(ns app.main
  "This namespace is used by the uberjar to start the system. This is
  a different NS from app.system, because we don't want to
  run :gen-class while in development, which sometimes results in class
  loader issues."
  (:gen-class)
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as c]
            [app.system :refer [make-system]]))

(defn -main
  [& _]
  (timbre/info "Starting system with properties: " (pr-str (System/getProperties)))
  (c/start (make-system)))
