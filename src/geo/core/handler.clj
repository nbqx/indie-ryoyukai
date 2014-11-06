(ns geo.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [response charset content-type header]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [hiccup.form :refer :all]
            [hiccup.element :refer [link-to]]
            [hiccup.middleware :refer [wrap-base-url]]
            [geo.parse :refer :all]))

(def head-tag
  [:head
   [:meta {:charset "utf-8"}]
   [:title "GPXからKMLにするやつ"]
   (include-css "/css/app.css")])

(def fork-me-button
  [:a {:href "https://github.com/nbqx/indie-ryoyukai"}
   [:img {:style "position: absolute; top: 0; left: 0; border: 0;"
          :src "https://camo.githubusercontent.com/82b228a3648bf44fc1163ef44c62fcc60081495e/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f6c6566745f7265645f6161303030302e706e67"
          :alt "Fork me on GitHub"
          :data-canonical-src "https://s3.amazonaws.com/github/ribbons/forkme_left_red_aa0000.png"}]])

(defn tpl [& body]
  (html5
   head-tag
   [:body
    ;; fork-me-button
    [:div {:id "main"}
     [:div {:class "form"} body]]]))

(defroutes app-routes

  (GET "/" [] (tpl 
               [:h1 "GPXからKMLにするやつ"]
               [:p {:style "margin-bottom:2em;"} "gpxをkmlに変換してgooglemapでつかうやつです"]
               (form-to {:enctype "multipart/form-data"} [:post "/parse"]
                        (file-upload {:id "gpxfile"} "file")
                        (submit-button {:id "btn"} "アップロード"))))

  (POST "/parse" {{{tempfile :tempfile filename :filename} :file} :params :as params}
        (try
          (if (nil? (re-find (re-matcher #"\.gpx" filename)))
            (tpl [:p "gpxファイルじゃないものをアップロードしてませんか?"]
                 [:p (link-to "/" "もどる")])
            (let [name (clojure.string/replace filename #"\.gpx" ".kml")
                  path (str (clojure.java.io/file tempfile))
                  kml (make-kml path)]
              (-> (response kml)
                  (content-type "application/vnd.google-earth.kml+xml")
                  (charset "UTF-8")
                  (header "Content-Disposition" (str "attachment; filename=\""
                                                     (java.net.URLEncoder/encode name)
                                                     "\"")))))
          (catch Exception e
            (let [error-name (.getName (class e))]
              (case error-name
                "javax.xml.stream.XMLStreamException" (str "Error: データの変換ができませんでした (" error-name ")") 
                (str "Error: " error-name))))))
  
  (route/not-found "Not Found"))

(def app
  (wrap-defaults
   app-routes
   ;; (wrap-base-url app-routes)
   ;; site-defaults))
   (-> site-defaults (assoc-in [:security :anti-forgery] false))))
