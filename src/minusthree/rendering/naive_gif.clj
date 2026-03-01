(ns minusthree.rendering.naive-gif
  (:require
   [clojure.spec.alpha :as s]
   [fastmath.vector :as v]
   [minusthree.engine.loading :as loading]
   [minusthree.engine.macros :refer [insert! s->]]
   [minusthree.engine.raw-data :as raw-data]
   [minusthree.engine.time :as time]
   [minusthree.engine.transform2d :as t2d]
   [minusthree.engine.types :as types]
   [minusthree.engine.utils :as utils :refer [raw-from-here]]
   [minusthree.engine.world :as world :refer [esse]]
   [minusthree.gl.cljgl :as cljgl]
   [minusthree.gl.gl-magic :as gl-magic]
   [minusthree.gl.texture :as texture]
   [minusthree.platform.jvm.sdl3-inputs :as inp]
   [minusthree.rendering.+render-data :as +render-data]
   [odoyle.rules :as o])
  (:import
   [org.lwjgl.opengl GL45]))

(def fbo-vs (raw-from-here "sprite-normal.vert"))
(def fbo-fs (raw-from-here "sprite.frag"))

(def horse-frames-png
  ["public/nondist/horse-0.png"
   "public/nondist/horse-1.png"
   "public/nondist/horse-2.png"
   "public/nondist/horse-3.png"
   "public/nondist/horse-4.png"
   "public/nondist/horse-5.png"])

(defn make-horse-anime [esse-id]
  (conj
   (into []
         (comp
          (map-indexed
           (fn [idx frame]
             [[idx ::texture/image (utils/get-image-from-resource frame)]
              [idx ::texture/for esse-id]]))
          cat)
         horse-frames-png)
   [esse-id ::texture/count (count horse-frames-png)]
   [esse-id ::texture/data {}]))

(defn create-naive-gif-gl []
  (let [program-info (cljgl/create-program-info-from-source fbo-vs fbo-fs)
        gl-summons     (gl-magic/cast-spell
                        [{:bind-vao :naive-gif}
                         {:buffer-data raw-data/plane3d-vertices :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :a_pos :use-shader program-info :count 3 :component-type GL45/GL_FLOAT}
                         {:buffer-data raw-data/plane3d-uvs :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :a_uv :use-shader program-info :count 2 :component-type GL45/GL_FLOAT}

                         {:unbind-vao true}])
        vao          (-> gl-summons ::gl-magic/data ::gl-magic/vao (get :naive-gif))]
    {:program-info program-info :vao vao}))


(defn horse-esse [world]
  (let [esse-id ::horse
        image-w 57
        image-h 32]
    (-> world
        (esse esse-id
              {::+render-data/data (create-naive-gif-gl)
               ::dimension (v/vec2 image-w image-h)}
              (loading/push #(make-horse-anime esse-id))))))

(defn init-fn [world _game]
  (horse-esse world))

(def ground-y -19.0)

(defn after-refresh [world _game]
  (-> world
      (o/insert ::horse
                {::horse-state :running
                 ::t2d/position (v/vec2 -32.0 ground-y)
                 ::t2d/scale (v/vec2 12.0 5.0)})))

(s/def ::dimension ::types/vec2)
(s/def ::horse-state #{:running :ascending :descending})
(s/def ::horse-action #{:jump})
(s/def ::horse-y-speed number?)

(defn jump-logic [dt position y-speed]
  (if (>= (.y position) ground-y)
    (let [dt (* dt 1e-2)
          gravity (* dt 0.2)
          jump-state (if (> y-speed 0) :ascending :descending)]
      {::horse-state jump-state
       ::horse-y-speed (- y-speed gravity)
       ::t2d/position (v/add position (v/vec2 0.0 (* dt y-speed)))})
    {::horse-y-speed 0.0
     ::horse-state :running
     ::t2d/position (v/vec2 -32.0 ground-y)}))

(def rules
  (o/ruleset
   {::input-mapping
    [:what
     [::inp/input keyname ::inp/keyup]
     :when (#{::inp/w ::inp/up} keyname)
     :then
     (insert! ::horse ::horse-action :jump)]

    ::horse-anime
    [:what
     [::time/now ::time/total tt]
     [::horse ::texture/data horse-frames]
     [::horse ::dimension dimension]
     [::horse ::t2d/position position]
     [::horse ::t2d/scale scale]
     [::horse ::+render-data/data render-data]
     [::horse ::horse-state horse-state]]

    ::horse-jump
    [:what
     [::horse ::horse-state :running]
     [::horse ::horse-action :jump]
     :then
     (s-> session
          (o/retract ::horse ::horse-action)
          (o/insert ::horse {::horse-state :ascending
                             ::horse-y-speed 2}))]

    ::horse-gravity
    [:what
     [::time/now ::time/delta dt]
     [::horse ::horse-state state {:then false}]
     [::horse ::horse-y-speed y-speed {:then false}]
     [::horse ::t2d/position position {:then false}]
     :when (not (= state :running))
     :then
     (insert! ::horse (jump-logic dt position y-speed))]}))

(def adhoc-horse-tint
  (float-array [0.8 0.6 0.6 0.7]))

(def frame-n (count horse-frames-png))
(def adhoc-animation-rate 125)

(defn render-world [world]
  (let [{:keys [tt horse-frames dimension render-data position scale horse-state]}
        (utils/query-one world ::horse-anime)]
    (when (and (= frame-n (count horse-frames)) render-data)
      (let [{:keys [program-info vao]} render-data
            curr   (case horse-state
                     :ascending 0 :descending 1
                     (int (Math/floor (mod (/ tt adhoc-animation-rate) frame-n))))
            frame  (:gl-texture (get horse-frames curr))
            p-s    (float-array (flatten [position scale]))]
        (GL45/glUseProgram (:program program-info))
        (GL45/glBindVertexArray vao)

        (GL45/glActiveTexture GL45/GL_TEXTURE0)
        (GL45/glBindTexture GL45/GL_TEXTURE_2D frame)
        (cljgl/set-uniform program-info :u_tex 0)

        (cljgl/set-uniform program-info :u_tint adhoc-horse-tint)
        (cljgl/set-uniform program-info :u_pos_scale p-s)
        (cljgl/set-uniform program-info :u_res (float-array dimension))
        (GL45/glDrawArrays GL45/GL_TRIANGLES 0 6)))))

(def system
  {::world/init-fn #'init-fn
   ::world/after-refresh #'after-refresh
   ::world/rules #'rules})

(comment

  :-)
