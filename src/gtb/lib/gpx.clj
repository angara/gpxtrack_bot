
(ns gtb.lib.gpx

  (:require
    [clojure.java.io      :refer [input-stream]]
    [clojure.xml          :refer [parse]]
    [clojure.zip          :refer [xml-zip]]
    [clojure.data.zip.xml :refer [xml-> xml1-> attr text]]
    ;
    [mlib.util            :refer [remove-nils]]))
;=


;; angle distance calculation

;; https://en.wikipedia.org/wiki/Earth_radius#Mean_radius
(def ^:const EARTH_RADIUS   6373.)
(def ^:const EARTH_DIAMETER (* 2 EARTH_RADIUS))

; GeoPy radius: 6372.795
;
; Mean Equatorial: 6,378.1370km
; Mean Polar: 6,356.7523
; Authalic/Volumetric: 6371008.8
; Meridional: 6367km

;; https://rosettacode.org/wiki/Haversine_formula#Java
(defn haversine [^double lat1 ^double lon1  ^double lat2 ^double lon2]
  (let [
        dlat (Math/toRadians (- lat2 lat1))
        dlon (Math/toRadians (- lon2 lon1))
        lat1 (Math/toRadians lat1)
        lat2 (Math/toRadians lat2)
        lat-sin (Math/sin (/ dlat 2))
        lon-sin (Math/sin (/ dlon 2))
        a (+ (* lat-sin lat-sin) (* lon-sin lon-sin (Math/cos lat1) (Math/cos lat2)))]
    (* EARTH_DIAMETER 
      (Math/asin (Math/sqrt a)))))
;;

(comment
  (haversine 36.12 -86.67  33.94 -118.40)  ;;=> 2886448.4297648543

  (haversine 
    53.061345 106.899986    ;; Кобылья Голова
    53.413352 107.789854)   ;; Хобой
    ;; => 70986.44268027513
  ,)

;; https://www.movable-type.co.uk/scripts/latlong.html
;; https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4666414/
;; https://www.topografix.com/gpx_manual.asp

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;


(defn parse-bytes [bytes]
  (let [xz (-> bytes input-stream parse xml-zip)]
        
    ;; (prn "gpx:" gpx)
    xz))
;;

;         trkpt (xml-> data :trk :trkseg :trkpt)]
;     (map #(vector (attr % :lat) 
;                   (attr % :lon) 
;                   (first (xml-> % :ele text))) trkpt)))

(defn tseg->points [tseg]
  ; (prn "tseg: " tseg)
  (->> (xml-> tseg :trkpt)
    (map
      #(remove-nils
          {
            :lat  (attr   % :lat)
            :lon  (attr   % :lon)
            :ele  (xml1-> % :ele  text)
            :sym  (xml1-> % :sym  text)
            :fix  (xml1-> % :fix  text)
            :time (xml1-> % :time text)
            :type (xml1-> % :type text)
            :name (xml1-> % :name text)
            :desc (xml1-> % :desc text)}))))
;;

(comment

  (let [gpx       (-> "tmp/test.gpx" slurp (.getBytes) parse-bytes)
        metadata  (xml1-> gpx :metadata)
        trk       (xml1-> gpx :trk)
        tseg      (xml-> trk :trkseg)]
    (prn "time:" (first (xml-> metadata :time text)))
    (prn "name:" (first (xml-> trk :name text)))
    ; (prn "tseg:" tseg))
    (tseg->points (first tseg)))

    ; (xml-> gpx :trk :trkseg :trkpt)
    ;gpx)   

  ,)

;;.
