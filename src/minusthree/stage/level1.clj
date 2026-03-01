(ns minusthree.stage.level1
  (:require
   [clojure.spec.alpha :as s]
   [fastmath.vector :as v]
   [minusthree.engine.macros :refer [insert!]]
   [minusthree.engine.time :as time]
   [minusthree.engine.transform2d :as t2d]
   [minusthree.engine.types :as types]
   [minusthree.engine.world :as world :refer [esse]]
   [minusthree.platform.jvm.sdl3-inputs :as inp]
   [minusthree.rendering.atlas :as atlas]
   [odoyle.rules :as o]))

;; hardcoded 1016 is the value from the size of atlas texture
(def stage-width 1016)
(def stage-height 1016)

(defn init-fn [world _game]
  (-> world
      (o/insert ::player ::t2d/position (v/vec2 0.0 0.0))))

(def trees
  ["foliagePack_006.png"
   "foliagePack_007.png"
   "foliagePack_008.png"
   "foliagePack_009.png"
   "foliagePack_010.png"])

(def bushes
  ["foliagePack_049.png"
   "foliagePack_050.png"
   "foliagePack_051.png"
   "foliagePack_052.png"])

(def rocks
  ["foliagePack_055.png"
   "foliagePack_061.png"])

(def terrains
  ["foliagePack_leaves_002.png"
   "foliagePack_leaves_010.png"])

(defn after-refresh [world _game]
  (-> world
      (esse ::cloud-a
            (atlas/foliage-instance
             {:tex-name "foliagePack_050.png"
              :scale (v/vec2 2.0 2.0)})
            {::atlas/layer 2
             ::atlas/tint [1.0 1.0 1.0 1.0]
             ::offset-pos (v/vec2 0.0 000.0)})
      (esse ::ground-a
            (atlas/foliage-instance
             {:tex-name "foliagePack_leaves_010.png"
              :scale (v/vec2 18.0 3.0)})
            {::atlas/layer 1
             ::atlas/tint [1.0 1.0 1.0 0.6]
             ::offset-pos (v/vec2 0.0 -900.0)})))

(s/def ::offset-pos ::types/vec2)

(def rules
  (o/ruleset
   {::control
    [:what
     [::time/now ::time/delta dt]
     [::inp/input keyname ::inp/keydown]
     [::player ::t2d/position player-pos {:then false}]
     :when
     (#{::inp/a ::inp/d} keyname)
     :then
     (let [speed   1016
           delta-s (* dt 1e-3)
           dist    (case keyname
                     ::inp/a (* -1 speed delta-s)
                     ::inp/d (* speed delta-s))
           move    (v/vec2 dist 0.0)]
       (insert! ::player ::t2d/position (v/add player-pos move)))]

    ::adhoc-player-pos
    [:what
     [::player ::t2d/position pos]
     [foliage-id ::atlas/layer _]
     [foliage-id ::offset-pos offset-pos]
     :then
     (insert! foliage-id ::t2d/position (v/add offset-pos pos))]}))

(def system
  {::world/init-fn #'init-fn
   ::world/after-refresh #'after-refresh
   ::world/rules #'rules})
