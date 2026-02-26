# thorvg + box2d + lwjgl in clojure

## repl dev

need jdk25, clojure
```
clojure -T:lets repl
```

## native libs

need babashka, local box2d at `../box2d` , thorvg at `../thorvg`, and their respective build tools

this will build local box2d and thorvg shared libs and put it inside `resources/public/libs`

```
bb -x bb/prep
```
