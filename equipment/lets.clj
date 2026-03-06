(ns equipment.lets
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [clojure.java.io :as io]))

(def game-coord 'self.chera/horsing-around)
(def game "HorsingAround")
(def version (format "0.2.%s" (b/git-count-revs nil)))
(def target-dir "target")
(def class-dir (str target-dir "/input/classes"))
(def dist-dir (str target-dir "/output"))
(def rel-dir (str dist-dir "/rel"))
(def base-uber-file (format "%s/jar/%s-%s.jar" dist-dir (name game-coord) version))

(defn os []
  (let [os-name (str/lower-case (System/getProperty "os.name"))]
    (cond
      (str/starts-with? os-name "windows") :windows
      ;;   (str/starts-with? os-name "mac")     :macos ;; no machine to try this
      (str/starts-with? os-name "linux")   :linux
      :else
      (throw (ex-info (str "os not supported yet! os: " os) {})))))

(defn decided-basis []
  (let [os-alias (os)]
    (b/create-basis {:project "deps.edn" :aliases [:jvm-game :repl os-alias]})))

(defn clean [& _]
  (println "cleaning target...")
  (b/delete {:path target-dir}))

(defn repl [& _]
  (println "running desktop game with repl...")
  (let [cmd (b/java-command {:basis (decided-basis) :main 'clojure.main :main-args ["-m" "minusthree.-dev.start"]})]
    (b/process cmd)))

(def minusthree-rel-windows (delay (b/create-basis {:project "deps.edn" :aliases [:release :windows]})))
(def minusthree-rel-linux (delay (b/create-basis {:project "deps.edn" :aliases [:release :linux]})))

(defn basis-by-os []
  (let [os-alias (os)]
    (case os-alias
      :windows minusthree-rel-windows
      :linux   minusthree-rel-linux)))

(defn minusthree-compile
  [{:keys [basis]}]
  (println "compiling minusthree clj sources...")
  (b/compile-clj {:basis @basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :ns-compile ['minusthree.platform.jvm.jvm-game]}))

(defn minusthree-uber
  [{:keys [basis uber-file]
    :or {uber-file base-uber-file
         basis (basis-by-os)}}]
  (b/delete {:path "target/output/jar"})
  (println "making an uberjar...")
  (b/write-pom {:lib game-coord
                :version version
                :basis @basis
                :src-dirs ["src"]
                :class-dir class-dir})
  (b/copy-dir {:src-dirs ["resources"]
               :target-dir class-dir})
  (minusthree-compile {:basis basis})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'minusthree.platform.jvm.jvm-game})
  (println "uberjar created at" uber-file)
  (println (str "run with `java -jar " uber-file "`")))

(defn uberjar [& _]
  (minusthree-uber {}))

(defn find-jar []
  (let [home "target/output/jar"]
    (first (filter #(str/ends-with? (.getName %) ".jar")
                   (file-seq (io/file home))))))

(defn play-jar [& _]
  (let [jre      "target/runtime"
        java     (str jre "/bin/java.exe")
        play-cmd [java "-jar" (.getAbsolutePath (find-jar))]]
    (println "Let's play the game! cmd:" play-cmd)
    (b/process {:out :inherit :command-args play-cmd})))

(defn play [& _]
  (let [game-exe (str "target/output/packr/" game ".exe")]
    (println "Let's play the game! cmd:" game-exe)
    (b/process {:out :inherit :command-args [game-exe]})))

(def java-modules
  ["java.base"])

(defn jlink [& _]
  (let [jre  "target/runtime"
        cmds ["jlink" "--add-modules" (str/join \, java-modules)
              "--no-header-files"
              "--no-man-pages"
              "--output" jre]
        java (str jre "/bin/java.exe")
        jtry [java "-jar" (.getAbsolutePath (find-jar))]]
    (b/delete {:path jre})
    (println "running" cmds)
    (b/process {:out :inherit :command-args cmds})
    (println "trying the game" jtry)
    (b/process {:out :inherit :command-args jtry})))

(defn packr [& _]
  (let [pack "target/output/packr"
        jre  "target/runtime"
        cmds ["java" "-jar" "packr-all-4.0.0.jar"
              "--platform" "windows64"
              "--jdk" jre
              "--executable" game
              "--classpath" (.getAbsolutePath (find-jar))
              "--mainclass" "minusthree.platform.jvm.jvm_game"
              "--output" pack]]
    (b/delete {:path pack})
    (println "running" cmds)
    (b/process {:out :inherit :command-args cmds})
    (b/zip {:src-dirs [pack] :zip-file (str "target/output/" game "-win.zip")})))

(defn release [& _]
  (uberjar)
  (packr))
