(ns zexif.core
  (:require [cljs.nodejs :as node]
            [cljs.core.async :as async :refer [<! >! chan put! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def fs (node/require "fs"))
(def walk (node/require "walk"))

(def ops (js-obj)) 
(set! (.-followLinks true))

(defn snif-exif [file out]
  )

(defn main []
  (let [walker (.walk walk "/tmp" ops)
        files (chan)]
    (.on walker "file" (fn [root stats next]
                         (go (>! files stats))
                         (next)))
    (go (loop [stat (<! files)]
          (.log js/console (str "File " (.-name stat)))
          (recur (<! files))))))

(set! *main-cli-fn* main)
