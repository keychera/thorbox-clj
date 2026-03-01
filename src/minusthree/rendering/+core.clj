(ns minusthree.rendering.+core
  (:require
   [minusthree.engine.world :as world]
   [minusthree.rendering.atlas :as atlas]
   [minusthree.rendering.gradientsky :as gradientsky]
   [minusthree.rendering.naive-gif :as naive-gif])
  (:import
   [org.lwjgl.opengl GL45]))

(def all
  [gradientsky/system
   naive-gif/system
   atlas/system])

(defn init [game]
  (GL45/glEnable GL45/GL_BLEND)
  (GL45/glEnable GL45/GL_CULL_FACE)
  (GL45/glEnable GL45/GL_MULTISAMPLE)
  #_(GL45/glEnable GL45/GL_DEPTH_TEST)
  game)

(defn rendering-zone [game]
  (let [{:keys [config]} game
        {:keys [w h]
         :or   {w 400
                h 10}}   (:window-conf config)
        world            (::world/this game)]
    (GL45/glBlendFunc GL45/GL_SRC_ALPHA GL45/GL_ONE_MINUS_SRC_ALPHA)
    (GL45/glClearColor 1.0 1.0 1.0 1.0)
    (GL45/glClear (bit-or GL45/GL_COLOR_BUFFER_BIT GL45/GL_DEPTH_BUFFER_BIT))
    (GL45/glViewport 0 0 w h)

    (gradientsky/render-world world)
    (atlas/render-world world #(<= (:layer %) 0))
    (naive-gif/render-world world)
    (atlas/render-world world #(> (:layer %) 0))

    game))

(defn destroy [game]
  game)
