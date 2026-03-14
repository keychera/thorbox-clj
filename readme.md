# horsing around

the source code for the game https://heritonaru.itch.io/horsing-around 

made for [Mini Jam 205: Horses](https://itch.io/jam/mini-jam-205-horses)

![horsing](horsing.gif)

## repl dev

need jdk25, clojure
```sh
clojure -T:lets repl
```

## make small runtime

```sh
clojure -T:lets uberjar
clojure -T:lets jlink
# or
clojure -T:lets play-jar
```

## release

```sh 
clojure -T:lets release
clojure -T:lets play
```
