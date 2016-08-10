(ns app.main
  "This namespace is used by the uberjar to start the system. This is
  a different NS from app.system, because we don't want to
  run :gen-class while in development, which sometimes results in class
  loader issues."
  (:gen-class)
  (:require [com.stuartsierra.component :as c]
            [app.system :refer [make-system]]))

(defn -main
  [& _]
  (c/start (make-system)))
