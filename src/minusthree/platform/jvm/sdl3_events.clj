(ns minusthree.platform.jvm.sdl3-events
  (:require
   [clojure.spec.alpha :as s]
   [minusthree.platform.jvm.sdl3-inputs :as sdl3-inputs])
  (:import
   [org.lwjgl.sdl SDLEvents]))

(s/def ::stop? some?)

(declare event-wiring)

(defn poll-events [game event-buf]
  (if (SDLEvents/SDL_PollEvent event-buf)
    (recur (event-wiring game event-buf) event-buf)
    (sdl3-inputs/process-input game)))

(defn event= [event-enum event]
  (= event-enum (.type event)))

(defn event-wiring [game event-buf]
  (condp event= event-buf

    SDLEvents/SDL_EVENT_QUIT
    (assoc game ::stop? true)

    SDLEvents/SDL_EVENT_KEY_UP
    (assoc game ::sdl3-inputs/event-keyup true)

    #_else game))
