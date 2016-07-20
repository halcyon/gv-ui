
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CSV -> CSV
;; Reformatting raw data from Blane, in particular: adding explicit `end` when possible
(do
    (require '[clojure.data.csv :as csv])
    (require '[clojure.java.io :as io])
    (require '[clojure.string :as str])
    (require '[clojure.set :as set]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Identifying distinct states

 (with-open [in-file (io/reader "../in.csv")]
     (let [csv-seq (doall (csv/read-csv in-file))]
       ;; split each
       (frequencies (into [] (comp (map (fn [[path _]] (str/split path #"-"))) cat) csv-seq))))


;; term   #
[["pdp" 5868]
 ["srp" 4019]
 ["media" 1719]
 ["home" 1553]
 ["generic" 1535]
 ["phone lead" 1503]  ;; "phone_lead"
 ["Floorplans" 1224]  ;; lowercase
 ["lead_floor_plans" 1186]
 ["email lead" 909]   ;; "email_lead"
 ["lead_contact_property_bottom" 381]
 ["lead_property_listings" 258]
 ["lead_media_div" 184]
 ["lead_thank_you_page" 83]
 ["login" 78]
 ["lead_spotlight" 61]
 ["social login" 18]  ;; "social_login"
 ["register" 9]
 ["lead_" 2]  ;; [?] -- discard these input rows
 ["lead_nearby_properties" 1]
 ["Path" 1] ;; [csv header] -- pass-thru
 ["lead_property_overview" 1]]




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; building our cleaned input file

(with-open [in-file (io/reader "../in.csv")
            out-file (io/writer "../munged.csv")]
  (let [csv-seq (csv/read-csv in-file)]
    (csv/write-csv out-file
                   (into []
                         (comp (take 30) ;
                            (keep-indexed
                             (fn [idx [path-str visits :as row]]
                               (if (= 0 idx)
                                 row
                                 (let [path* (str/split path-str #"-")
                                       path (cond-> path* (< (count path*) 5) (conj "end"))
                                       pages (into #{} path)
                                       ignore #{"lead_"}
                                       rename {"phone lead" "phone_lead"
                                               "email lead" "email_lead"
                                               "social login" "social_login"
                                               "Floorplans"  "floorplans"}]
                                   (when (empty? (set/intersection pages ignore))
                                     (let [renamed-path (apply str (interpose \- (map #(get rename % %) path)))]
                                       [renamed-path visits])))))))
                         csv-seq))))
