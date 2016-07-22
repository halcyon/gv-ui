(ns app.api
  (:require [om.next.server :as om]
            [om.next.impl.parser :as op]
            [taoensso.timbre :as timbre]))

(defmulti apimutate om/dispatch)
(defmulti api-read om/dispatch)

(defmethod apimutate :default [e k p]
  (timbre/error "Unrecognized mutation " k))

(defmethod apimutate 'app/add-item [{:keys [db]} k {:keys [id label]}]
  {:action (fn []
             (let [items (:items db)
                   next-id (:next-id db)
                   new-id (swap! next-id inc)]
               (swap! items conj {:id new-id :label label})
               {:tempids {id new-id}}))})

(defmethod api-read :default [{:keys [ast query] :as env} dispatch-key params]
  (timbre/error "Unrecognized query " (op/ast->expr ast)))

(defmethod api-read :all-items [{:keys [db] :as env} dispatch-key params]
  {:value @(:items db)})

(defmethod api-read :all-tables [{:keys [db] :as env} dispatch-key params]
  (let [res (vals @(:tables db))]
    ;; (timbre/info ":all-tables" res)
    {:value (into [] (vals @(:tables db)))}))

;; (in-ns 'untangled.server.impl.components.handler)
;; (defn valid-response?
;;   [result]
;;   (timbre/info "valid-response? arg: " result)
;;   (and
;;     (not (instance? Exception result))
;;     (not (some (fn [[_ {:keys [om.next/error]}]] (some? error)) result))))
;; (in-ns 'app.api)
