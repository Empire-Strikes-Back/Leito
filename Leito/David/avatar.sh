#!/bin/bash

repl(){
  npm i --no-package-lock
  mkdir -p out
  cp src/Leito/David/index.html out/index.html
  cp src/Leito/David/style.css out/style.css
  cp package.json out/package.json
  clj -A:Moana -M -m shadow.cljs.devtools.cli clj-repl
  # (shadow/watch :ui)
  # (shadow/repl :ui)
  # :repl/quit
}

release(){
  rm -rf out
  npm i --no-package-lock
  mkdir -p out
  cp src/Leito/David/index.html out/index.html
  cp src/Leito/David/style.css out/style.css
  clojure -A:Moana -M -m shadow.cljs.devtools.cli release main
}

"$@"