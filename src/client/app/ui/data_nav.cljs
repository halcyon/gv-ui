(ns app.ui.data-nav
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]))

(defui ^:once DataPoint
  static om/Ident
  (ident [this {:keys [id] :as props}] [:point/by-id id])
  static om/IQuery
  (query [this] [:id :x :y :t])
  Object
  (render [this]
          (let [ps (om/props this)]
            (dom/li nil (pr-str ps)))))
(def ui-data-point (om/factory DataPoint {:keyfn :id}))

(defui ^:once DataSeries
  static uc/InitialAppState
  (initial-state [clz params]
                 {:id 1
                  :points [{:id 0 :t 0 :x 0 :y 0}
                           {:id 1 :t 1 :x 1 :y 1}
                           {:id 2 :t 2 :x 1 :y 2}
                           {:id 3 :t 3 :x 2 :y 3}
                           {:id 4 :t 4 :x 2 :y 3}
                           {:id 5 :t 5 :x 3 :y 5}]})
  static om/Ident
  (ident [this {:keys [id] :as props}] [:series/by-id id])
  static om/IQuery
  (query [this] [:id {:points (om/get-query DataPoint)}])
  Object
  (render [this]
          (let [{:keys [id points]} (om/props this)]
            (dom/ol nil (str "data series " id ": ")
                    (map ui-data-point points)))))
(def ui-data-series (om/factory DataSeries {:keyfn :id}))

(defui ^:once DataNav
  static uc/InitialAppState
  (initial-state [clz params] {:id 1 :series [(uc/initial-state DataSeries {})]})
  static om/Ident
  (ident [this {id :id :as props}] [:nav/by-id id])
  static om/IQuery
  (query [this] [:id {:series (om/get-query DataSeries)}])
  Object
  (render [this]
          (let [{:keys [id series]} (om/props this)]
            (dom/div nil
                     (dom/b nil (str "DataNav " id ": "))
                     (map ui-data-series series)))))

(def ui-data-nav (om/factory DataNav {:keyfn :id}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Take 3


(def legend-display
  {"end"                          "End"
   "home"                         "Home"
   "srp"                          "SRP"
   "pdp"                          "PDP"
   "generic"                      "Generic"
   "phone_lead"                   "Phone Lead"
   "email_lead"                   "Email Lead"
   "media"                        "Media"
   "lead_media_div"               "Lead: Media"
   "floorplans"                   "Floorplans"
   "lead_floor_plans"             "Lead: Floorplans"
   "login"                        "Login"
   "register"                     "Register"
   "social_login"                 "Social Login"
   "lead_property_listings"       "Lead: Prop. Listings"
   "lead_property_thank_you_page" "Lead: Thank You Page"
   "lead_property_spotlight"      "Lead: Spotlight"
   "lead_nearby_properties"       "Lead: Nearby Properties"
   "lead_property_overview"       "Lead: Property Overview"
   "lead_contact_property_bottom" "Lead: Contact Property"})

(def legend-colors
  {"end" "#C1BAA9"
   "home" "#5C8100"
   "srp" "#156B90"
   "pdp" "#0F8C79"
   "generic" "#95A17E"
   "phone_lead" "#F2DA57"
   "email_lead" "#BD2D28"
   "media" "#684664"
   "lead_media_div" "#8E6C8A"
   "floorplans" "#BA5F06"
   "lead_floor_plans" "#E6842A"
   "login" "#A0B700"
   "register" "#A0B700"
   "social_login" "#A0B700"
   "lead_property_listings" "#E3BA22"
   "lead_property_thank_you_page" "#E3BA22"
   "lead_property_spotlight" "#E3BA22"
   "lead_nearby_properties" "#E3BA22"
   "lead_property_overview" "#E3BA22"
   "lead_contact_property_bottom" "#E3BA22"})


(defui ^:once PathStep
  static om/Ident
  (ident [this props] [])
  static om/IQuery
  (query [this] [])
  Object
  (render [this]
          (let [{:keys []} (om/props this)]
            (dom/b nil ""))))

(defui ^:once Path
  Object
  (render [this]
          (let [{:keys [path]} (om/props this)]
            (dom/div nil
                     (map (fn [step]
                            (dom/b #js {:className "path-step label label-large"
                                        :style #js {:color "#FFFFFF"
                                                    :backgroundColor (get legend-colors step "#F2DA57")}}
                                   (get legend-display step)))
                          path)))))
(def ui-path (om/factory Path))

(defui ^:once TableRow
  static uc/InitialAppState
  (initial-state [clz params] (merge {:id [0 0] :path ["path" "to" "nowhere"] :count 42} params))
  static om/Ident
  (ident [this props] [:table.row/by-id (:id props)])
  static om/IQuery
  (query [this] [:id :path :count])
  Object
  (render [this]
          (let [{:keys [path count] :as props} (om/props this)]
            (dom/tr #js {:className "table-row"}
                     (dom/td #js {:className "path-path"} (ui-path props))
                     (dom/td #js {:className "path-count"} count)))))
(def ui-table-row (om/factory TableRow))

(defui ^:once TableHeader
  static uc/InitialAppState
  (initial-state [clz params] (merge {:id [0 0] :cols ["Feed" "Me" "Seymour"]} params))
  static om/Ident
  (ident [this props] [:table.header/by-id (:id props)])
  static om/IQuery
  (query [this] [:id :cols])
  Object
  (render [this]
          (let [{:keys [cols]} (om/props this)
                [path count] cols]
            (dom/tr nil
                    (dom/td #js {:className "table-header"} path)
                    (dom/td #js {:className "table-header"} count)))))
(def ui-table-header (om/factory TableHeader))

(defui ^:once DataTable
  static uc/InitialAppState
  (initial-state [clz params] {:id     0
                               :header (uc/initial-state TableHeader {:id [0 0] :cols ["Sequence" "Count"]})
                               :rows   [(uc/initial-state TableRow {:id [0 0] :path ["srp" "pdp" "end"] :count 2000})
                                        (uc/initial-state TableRow {:id [0 1] :path ["pdp" "pdp" "end"] :count 210})
                                        (uc/initial-state TableRow {:id [0 2] :path ["pdp" "home" "end"] :count 20})
                                        (uc/initial-state TableRow {:id [0 3] :path ["home" "srp" "pdp" "srp" "end"] :count 50})
                                        (uc/initial-state TableRow {:id [0 4] :path ["pdp" "srp" "end"] :count 30})]})
  static om/Ident
  (ident [this props] [:table/by-id (:id props)])
  static om/IQuery
  (query [this] [:id {:header (om/get-query TableHeader)} {:rows (om/get-query TableRow)}])
  Object
  (render [this]
          (let [{:keys [id header rows]} (om/props this)]
            (dom/div nil (dom/b nil "DataTable: " id))
            (dom/table #js {:className "data-table"}
                       (dom/tbody nil
                                  (ui-table-header header)
                                  (map ui-table-row rows))))))
(def ui-table (om/factory DataTable {:keyfn :id}))

(defui ^:once DataTab
  static uc/InitialAppState
  (initial-state [clz params] {:id 1, :type :data-tab, :content (uc/initial-state DataTable {})})
  static om/Ident
  (ident [this {id :id :as props}] [:data-tab id])
  static om/IQuery
  (query [this] [:id :type {:content (om/get-query DataTable)}])
  Object
  (render [this]
          (let [{:keys [content]} (om/props this)]
            (dom/div nil
                     (map ui-table content)))))
(def ui-tab (om/factory DataTab {:keyfn (juxt :type :id)}))
