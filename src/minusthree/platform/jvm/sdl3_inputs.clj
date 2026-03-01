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

(s/def ::event-keyup some?)

(defn pressed? [^ByteBuffer keyboard-state scancode]
  (not= (.get keyboard-state scancode) 0))

(let [allowed-keys {::w SDLScancode/SDL_SCANCODE_W
                    ::a SDLScancode/SDL_SCANCODE_A
                    ::s SDLScancode/SDL_SCANCODE_S
                    ::d SDLScancode/SDL_SCANCODE_D
                    ::p SDLScancode/SDL_SCANCODE_P}]
  (def keyname->scancode allowed-keys)
  (doseq [inp (keys allowed-keys)]
    (utils/defspec inp #{::keyup ::keydown}))
  (def default-facts
    (into {} (map (fn [k] [k ::keyup])) (keys allowed-keys))))

(defn process-input [{previously-pressed? ::pressed? :as prev-game}]
  (let [kb-state (SDLKeyboard/SDL_GetKeyboardState)
        game'    (assoc prev-game ::pressed? #{})]
    (-> (reduce-kv
         (fn [game keyname scancode]
           (if (pressed? kb-state scancode)
             (-> game
                 (update ::world/this o/insert ::input keyname ::keydown)
                 (update ::pressed? conj keyname))
             (if (and (::event-keyup game) (previously-pressed? keyname))
               (update game ::world/this o/insert ::input keyname ::keyup)
               game)))
         game' keyname->scancode)
        (dissoc ::event-keyup))))

(defn init-fn [world _game]
  (o/insert world ::input default-facts))

(def system
  {::world/init-fn #'init-fn})
