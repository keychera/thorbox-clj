(ns equipment.lets
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [clojure.java.io :as io]))

(def game 'self.chera/horsing-around)
(def version (format "0.2.%s" (b/git-count-revs nil)))
(def target-dir "target")
(def class-dir (str target-dir "/input/classes"))
(def dist-dir (str target-dir "/output"))
(def rel-dir (str dist-dir "/rel"))
(def base-uber-file (format "%s/jar/%s-%s.jar" dist-dir (name game) version))

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
  (println "making an uberjar...")
  (b/write-pom {:lib game
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

(defn release [& _]
  (minusthree-uber {}))

(defn jpackage [& _]
  ;; jpackage makes an installer that is unintuitive atm
  (let [home "target/output/jar"
        jar  (first
              (eduction
               (filter #(str/ends-with? (.getName %) ".jar"))
               (file-seq (io/file home))))
        cmds ["jpackage"
              "--input" home
              "--name" "HorsingAround"
              "--main-jar" (.getName jar)
              "--description" "a game submission for Mini Jam 205: Horses"
              "--java-options" "--enable-native-access=ALL-UNNAMED"]]
    (println "running" cmds)
    (b/process {:out :inherit :command-args cmds})))


(defn packr [& _]
  (b/delete {:path "target/output/packr"})
  (let [home "target/output/jar"
        jar  (first
              (eduction
               (filter #(str/ends-with? (.getName %) ".jar"))
               (file-seq (io/file home))))
        cmds ["java" "-jar" "packr-all-4.0.0.jar"
              "--platform" "windows64"
              "--jdk" "OpenJDK25U-jre_x64_windows_hotspot_25.0.2_10.zip"
              "--executable" "HorsingAround"
              "--classpath" (.getAbsolutePath jar)
              "--mainclass" "minusthree.platform.jvm.jvm_game"
              "--output" "target/output/packr"]]
    (println "running" cmds)
    (b/process {:out :inherit :command-args cmds})))
