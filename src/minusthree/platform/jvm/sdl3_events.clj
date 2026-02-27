(ns minusthree.platform.jvm.sdl3-events
  (:require
   [clojure.spec.alpha :as s])
  (:import
   [org.lwjgl.sdl SDLEvents]))

(s/def ::stop? some?)

(declare event-wiring)

(defn poll-events [game event-buf]
  (if (SDLEvents/SDL_PollEvent event-buf)
    (recur (event-wiring game event-buf) event-buf)
    game))

(defn event= [event-enum event]
  (= event-enum (.type event)))

(defn event-wiring [game event-buf]
  (condp event= event-buf

    SDLEvents/SDL_EVENT_QUIT (assoc game ::stop? true)

    #_else game))
