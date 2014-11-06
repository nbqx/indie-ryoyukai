(ns geo.parse
  (:require [mercator.gpx :refer :all]
            [hiccup.core :refer :all]
            [hiccup.page :refer [xml-declaration]]))

(defn- parse! [gpx-file]
  (let [data (-> gpx-file
                 (clojure.java.io/file)
                 (clojure.java.io/input-stream)
                 (parse))]
    (first (:features data))))

(defn- get-name [parsed-data]
  (get-in parsed-data [:properties :name]))

(defn- get-coordinates [parsed-data]
  (get-in parsed-data [:geometry :coordinates]))

;; usage: (spit "out.kml" (make-kml "/path/to/gpx"))
(defn make-kml [^String gpx-file-path]
  (let [data (parse! gpx-file-path)
        coordinates (get-coordinates data)
        name (get-name data)]
    (html (xml-declaration "UTF-8")
          [:kml {:xmlns "http://www.opengis.net/kml/2.2"
                 (keyword "xmlns:atom") "http://www.w3.org/2005/Atom"}
           [:Document
            [:name name]
            [(keyword "atom:author") [(keyword "atom:name") ""]]
            [:description ""]
            [:Placemark
             [:Style [:LineStyle [:color "99ff0000"] [:width 6]]]
             [:LineString [:coordinates
                           (clojure.string/join ","
                                                (map #(clojure.string/join "," %) coordinates))]]]]])))




