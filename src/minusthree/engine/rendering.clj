(ns minusthree.engine.rendering
  (:require
   [minusthree.engine.rendering.imgui :as imgui])
  (:import
   [org.lwjgl.opengl GL45]))

(defn init [game]
  (GL45/glEnable GL45/GL_BLEND)
  (GL45/glEnable GL45/GL_BLEND)
  (GL45/glEnable GL45/GL_CULL_FACE)
  (GL45/glEnable GL45/GL_MULTISAMPLE)
  (GL45/glEnable GL45/GL_DEPTH_TEST)
  (-> game
      (imgui/init)))

(defn rendering-zone [game]
  (let [{:keys [config]} game
        {:keys [w h]
         :or {w 400 h 10}}    (:window-conf config)]
    (GL45/glBlendFunc GL45/GL_SRC_ALPHA GL45/GL_ONE_MINUS_SRC_ALPHA)
    (GL45/glClearColor (/ 0x38 0xff) (/ 0x32 0xff) (/ 0x2c 0xff) 1.0)
    (GL45/glClear (bit-or GL45/GL_COLOR_BUFFER_BIT GL45/GL_DEPTH_BUFFER_BIT))
    (GL45/glViewport 0 0 w h)

    (imgui/frame game))
  game)

(defn destroy [game]
  (doto game
    (imgui/destroy)))
