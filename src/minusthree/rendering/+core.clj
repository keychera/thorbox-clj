(ns minusthree.rendering.+core
  (:require
   [minusthree.engine.utils :as utils]
   [minusthree.engine.world :as world]
   [minusthree.rendering.atlas :as atlas]
   [minusthree.rendering.gradientsky :as gradientsky])
  (:import
   [org.lwjgl.opengl GL45]))

(def all
  [gradientsky/system
   atlas/system])

(defn init [game]
  (GL45/glEnable GL45/GL_BLEND)
  (GL45/glEnable GL45/GL_CULL_FACE)
  (GL45/glEnable GL45/GL_MULTISAMPLE)
  (GL45/glEnable GL45/GL_DEPTH_TEST)
  game)

(defn rendering-zone [game]
  (let [{:keys [config]} game
        {:keys [w h]
         :or {w 400 h 10}} (:window-conf config)
        world (::world/this game)]
    (GL45/glBlendFunc GL45/GL_SRC_ALPHA GL45/GL_ONE_MINUS_SRC_ALPHA)
    (GL45/glClearColor 1.0 1.0 1.0 1.0)
    (GL45/glClear (bit-or GL45/GL_COLOR_BUFFER_BIT GL45/GL_DEPTH_BUFFER_BIT))
    (GL45/glViewport 0 0 w h)

    (atlas/render-world world)
    ;; why the subsequent render disappear?

    (let [{:keys [render-data]} (utils/query-one world ::gradientsky/render-data)
          {::gradientsky/keys [program-info vao]} render-data]
      (GL45/glUseProgram (:program program-info))
      (GL45/glBindVertexArray vao)
      (GL45/glDrawArrays GL45/GL_TRIANGLES 0 6))

    game))

(defn destroy [game]
  game)
