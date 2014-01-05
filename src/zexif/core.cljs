(ns zexif.core
  (:require [cljs.nodejs :as node]
            [cljs.core.async :as async :refer [<! >! chan put! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def fs (node/require "fs"))
(def walk (node/require "walk"))
(def extractif (node/require "extractif"))

(def ops (js-obj)) 
(set! (.-followLinks true))

(defn snif-exif [file]
  (let [out (chan)]
    (extractif file
               (fn [err exif]
                 (go
                  (>! out {:file file :err err :exif exif}))))
    out))

(defn clojurize-exif [src]
  (when-let [exif (:exif src)]
    (assoc src :exif (js->clj exif :keywordize-keys true))))

(defn main []
  (let [walker (.walk walk "/Volumes/data/Alba" ops)
        files (chan)
        exif-data (chan 500)]
    (.on walker "file" (fn [root stats next]
                         (go (>! files {:root root :stat stats})
                             (next))))
    (go (loop [{:keys [root stat]} (<! files)]
          (let [file (str root "/" (.-name stat))
                exif-ch (snif-exif file)
                data (<! exif-ch)
                m (clojurize-exif data)]
            (prn m)
;;            (>! exif-data data)
            (recur (<! files)))))))

(set! *main-cli-fn* main)
