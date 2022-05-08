#!/bin/bash

#!/bin/bash


repl(){
  clj \
    -J-Dclojure.core.async.pool-size=1 \
    -X:repl Ripley.core/process \
    :main-ns Leito.main
}

main(){
  clojure \
    -J-Dclojure.core.async.pool-size=1 \
    -M -m Leito.main
}

ui(){
  # watch release clj-repl
  npm i --no-package-lock
  mkdir -p out/ui/
  cp src/Leito/index.html out/ui/index.html
  cp src/Leito/style.css out/ui/style.css
  clj -A:Moana:ui -M -m shadow.cljs.devtools.cli $1 ui
  # (shadow/watch :ui)
  # (shadow/repl :ui)
  # :repl/quit
}

ui_release(){
  rm -rf out/ui
  ui release
}

tag(){
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  TAG="$COMMIT_COUNT-$COMMIT_HASH"
  git tag $TAG $COMMIT_HASH
  echo $COMMIT_HASH
  echo $TAG
}

jar(){

  clojure \
    -X:identicon Zazu.core/process \
    :word '"Leito"' \
    :filename '"out/identicon/icon.png"' \
    :size 256

  rm -rf out/*.jar
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  clojure \
    -X:uberjar Genie.core/process \
    :main-ns Leito.main \
    :filename "\"out/Leito-$COMMIT_COUNT-$COMMIT_HASH.jar\"" \
    :paths '["src" "out/ui"]'
}

release(){
  jar
}

# https://github.com/nrepl/nrepl/blob/master/doc/modules/ROOT/pages/usage/server.adoc

build(){
    clojure -A:fig:local:build
}

fig_dev(){
    clojure \
    -Sdeps '{:deps {nrepl {:mvn/version "0.6.0"} cider/cider-nrepl {:mvn/version "0.21.1"} cider/piggieback {:mvn/version "0.4.1"} figwheel-sidecar {:mvn/version "0.5.18"}}}' \
    -A:fig:local -m nrepl.cmdline  --middleware "[cider.nrepl/cider-middleware cider.piggieback/wrap-cljs-repl]" --bind "0.0.0.0" --port 7888
}

fig_clean(){
    rm -rf target .cpcache
}

clean(){
    rm -rf .shadow-cljs node_modules .cpcache resources/public/js-out
}

shadow(){
    ./node_modules/.bin/shadow-cljs "$@"
}

prod(){
    npm i
    shadow -A:shadow:dev:prod release program
}

dev(){
    npm i
    shadow -A:shadow:dev watch program
    # npx shadow-cljs -A:dev:local watch program
    # yarn dev
}

prod-local(){
    npm i
    shadow -A:prod:local:cache release program
    # yarn prod
}

server(){
    shadow-cljs -A:shadow:server server
    # yarn server
}

"$@"