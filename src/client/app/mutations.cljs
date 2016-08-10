(ns app.mutations
  (:require [untangled.client.mutations :as m]
            [untangled.client.core :as uc]
            [app.ui.todo :as todo]
            [app.ui.d3 :as app.d3]
            [om.next :as om]))

(defmethod m/mutate 'app/add-item [{:keys [state ref]} k {:keys [id label]}]
  {:remote true
   :action (fn []
             (let [list-path (conj ref :items)
                   new-item (uc/initial-state todo/Item {:id id :label label})
                   item-ident (om/ident todo/Item new-item)]
               ; place the item in the db table of items
               (swap! state assoc-in item-ident new-item)
               ; tack on the ident of the item in the list
               (uc/integrate-ident! state item-ident :append list-path)))})

(defmethod m/mutate 'fetch/items-loaded [{:keys [state]} _ _]
  {:action (fn []
             (let [idents (get @state :all-items)]
               (swap! state (fn [s]
                              (-> s
                                  (assoc-in [:lists/by-title "Next Steps?" :items] idents)
                                  (dissoc :all-items))))))})


(defmethod m/mutate 'app/choose-tab
  [{:as env
    :keys [state ;; app-state atom
           ref]} ;; ident of component on which mutation is run

   dispatch-key ;; 'app/choose-tab
   {:keys [tab content] :as params}]

  {:action (fn [] (swap! state
                         #(let [st %]
                            (js/console.log "tabsw. params" params)
                            (js/console.log "tabsw. state:" st)
                            (cond-> st

                              ;; always change tab
                              true (assoc-in [:tabs 0] tab)


                              ;; janky routing
                              true (assoc-in [:tabs 1] (if content content 1))


                              ;; TODO - generalize --------,-,--,        ;; JANK workaround for not having SRP tab initialized on initial app load
                              ;; (some? content)
                              ;; (update-in [tab content] merge {:type :sunburst-tab
                              ;;                                 :content content
                              ;;                                 :id content
                              ;;                                 :new-shiny (om/tempid)})
                              ))))})


(defmethod m/mutate 'd3/add-square [{:keys [state ref]} k params]
  {:action (fn []
             (let [square-path (conj ref :data :squares)
                   new-square (uc/initial-state app.d3/Square {})
                   square-ident (om/ident app.d3/Square new-square)]
               ; place the square in the db table of squares
               (swap! state assoc-in square-ident new-square)
               ; tack on the ident of the item in the list
               (uc/integrate-ident! state square-ident :append square-path)))})

(defmethod m/mutate 'd3/rem-square [{:as env :keys [state ref]} k {:keys [id]}]
  {:action (fn []
             ;; `ref` will be nil if `om/transact!` is called on the reconciler
             (assert ref "ref cannot be nil")
             (let [clear-all? (= id :all)
                   squares-path (conj ref :data :squares)]
               (if clear-all?
                 (swap! state (fn [s]
                                (-> s
                                    (update :square/by-id empty)
                                    (update-in squares-path empty))))
                 (swap! state (fn [s]
                                (-> s
                                    (update :square/by-id dissoc id)
                                    (update-in squares-path (partial into [] (remove (comp #{id} second))))))))))})

(defmethod m/mutate 'fetch/initial-load [{:keys [state]} _ _]
  {:action (fn []
             (let [tbl-idents (get @state :all-tables)
                   viz-idents (get @state :all-viz)]
               (swap! state
                      (fn [s]
                        (-> s
                            (assoc-in [:data-tab 1 :content] tbl-idents)
                            (assoc-in [:data-viz-tab 1 :content] viz-idents)
                            (dissoc :all-tables :all-viz))))))})
