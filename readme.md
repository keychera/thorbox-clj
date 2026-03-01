# ~~thorvg + box2d~~ + lwjgl in clojure

thorvg and box2d disabled for now since I am sensing a problem brewing during distribution

## repl dev

need jdk25, clojure
```
clojure -T:lets repl
```

## release

```
clojure -T:lets release
```

## native libs

need babashka, local box2d at `../box2d` , thorvg at `../thorvg`, and their respective build tools

this will build local box2d and thorvg shared libs and put it inside `resources/public/libs`

```
bb -x bb/prep
```
