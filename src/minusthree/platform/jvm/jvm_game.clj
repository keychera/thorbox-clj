(ns minusthree.platform.jvm.jvm-game
  (:require
   [minusthree.platform.jvm.sdl3 :as sdl3])
  (:gen-class))

(defn create-window [config]
  (sdl3/create-window config))

(defn start [window config]
  (sdl3/start-sdl3-loop window config))

(defn -main [& _]
  ;; config needs hammock
  (let [config {:window-conf {:w 540 :h 540 :x 100 :y 100 :floating? false}
                :imgui       {:title "Hello Miku from Clojure!"
                              :text  "looking cute as usual!"}}
        window (#'create-window (:window-conf config))]
    (#'start window config)))
