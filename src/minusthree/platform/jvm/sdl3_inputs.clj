(ns minusthree.platform.jvm.sdl3-inputs
  (:require
   [clojure.spec.alpha :as s]
   [minusthree.engine.utils :as utils]
   [minusthree.engine.world :as world]
   [odoyle.rules :as o])
  (:import
   [java.nio ByteBuffer]
   [org.lwjgl.sdl SDLKeyboard SDLScancode]))

(s/def ::keyup some?)
(s/def ::keydown some?)

(defn get-key [scancode ^ByteBuffer keyboard-state]
  (not= (.get keyboard-state scancode) 0))

(let [allowed-key [::w ::a ::s ::d ::p]]
  (doseq [inp allowed-key]
    (utils/defspec inp #{::keyup ::keydown}))
  (def default-facts
    (into {} (map (fn [k] [k ::keyup])) allowed-key)))

(defn process-input [game press-state]
  (let [kb-state (SDLKeyboard/SDL_GetKeyboardState)
        key-word (condp get-key kb-state
                   SDLScancode/SDL_SCANCODE_W ::w
                   SDLScancode/SDL_SCANCODE_A ::a
                   SDLScancode/SDL_SCANCODE_S ::s
                   SDLScancode/SDL_SCANCODE_D ::d
                   SDLScancode/SDL_SCANCODE_P ::p
                   nil)]
    (if key-word
      (update game ::world/this o/insert ::input key-word press-state)
      game)))

(defn init-fn [world _game]
  (o/insert world ::input default-facts))

(def system
  {::world/init-fn #'init-fn})
