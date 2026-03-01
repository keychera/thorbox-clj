(ns minusthree.rendering.gradientsky
  (:require
   [minusthree.engine.raw-data :as raw-data]
   [minusthree.engine.utils :as utils :refer [raw-from-here]]
   [minusthree.engine.world :as world]
   [minusthree.gl.cljgl :as cljgl]
   [minusthree.gl.gl-magic :as gl-magic]
   [minusthree.rendering.+render-data :as +render-data]
   [odoyle.rules :as o])
  (:import
   [org.lwjgl.opengl GL45]))

(def fbo-vs (raw-from-here "sky.vert"))
(def fbo-fs (raw-from-here "sky.frag"))

(defn hex->rgb [hex-int]
  (let [r (float (/ (bit-and (bit-shift-right hex-int 16) 0xFF) 0xFF))
        g (float (/ (bit-and (bit-shift-right hex-int 8) 0xFF) 0xFF))
        b (float (/ (bit-and hex-int 0xFF) 0xFF))]
    (java.lang.String/format  
     java.util.Locale/ENGLISH
     "vec3 color = vec3(%.2f, %.2f, %.2f);" 
     (object-array [r g b]))))

(comment
  ;; hardcoding purposes

  (println (hex->rgb 0xA5DEE5))
  (println (hex->rgb 0xE8FAF5))

  :-)

(defn create-sky-gl []
  (let [program-info (cljgl/create-program-info-from-source fbo-vs fbo-fs)
        gl-summons     (gl-magic/cast-spell
                        [{:bind-vao "sky"}
                         {:buffer-data raw-data/plane3d-vertices :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :a_pos :use-shader program-info :count 3 :component-type GL45/GL_FLOAT}
                         {:buffer-data raw-data/plane3d-uvs :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :a_uv :use-shader program-info :count 2 :component-type GL45/GL_FLOAT}
                         {:unbind-vao true}])
        vao          (-> gl-summons ::gl-magic/data ::gl-magic/vao (get "sky"))]
    {:program-info program-info :vao vao}))

(defn after-refresh [world _game]
  (let [sky-gl (create-sky-gl)]
    (o/insert world ::gradientsky ::+render-data/data sky-gl)))

(defn before-refresh [world _game]
  (let [{:keys [render-data]} (utils/query-one world ::sky-render)
        {:keys [program-info vao]} render-data]
    (GL45/glDeleteVertexArrays vao)
    (GL45/glDeleteProgram (:program program-info))))

(def rules
  (o/ruleset
   {::sky-render
    [:what
     [::gradientsky ::+render-data/data render-data]]}))

(defn render-world [world]
  (let [{:keys [render-data]} (utils/query-one world ::sky-render)
        {:keys [program-info vao]} render-data]
    (GL45/glUseProgram (:program program-info))
    (GL45/glBindVertexArray vao)
    (GL45/glDrawArrays GL45/GL_TRIANGLES 0 6)))

(def system
  {::world/after-refresh #'after-refresh
   ::world/rules #'rules
   ::world/before-refresh #'before-refresh})
