# horsing around

the source code for the game https://heritonaru.itch.io/horsing-around 

made for [Mini Jam 205: Horses](https://itch.io/jam/mini-jam-205-horses)

![horsing](horsing.gif)

this project was named thorbox because I originally planned to use thorvg and box2d, but I scrapped that idea midway because I felt like I will encounter a problem during distribution for the game jam above.

## repl dev

need jdk25, clojure
```
clojure -T:lets repl
```

## release

```
clojure -T:lets release
clojure -T:lets packr
```

## native libs (unused)

need babashka, local box2d at `../box2d` , thorvg at `../thorvg`, and their respective build tools

this will build local box2d and thorvg shared libs and put it inside `resources/public/libs`

```
bb -x bb/prep
```
