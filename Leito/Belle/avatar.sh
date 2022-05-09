#!/bin/bash


repl(){
  clj \
    -J-Dclojure.core.async.pool-size=1 \
    -X:repl Ripley.core/process \
    :main-ns Leito.Belle.main
}

main(){
    # shadow-cljs -A:shadow:server server
    clj -M -m shadow.cljs.devtools.cli server
}

jar(){

  rm -rf out/*.jar
  # COMMIT_HASH=$(git rev-parse --short HEAD)
  # COMMIT_COUNT=$(git rev-list --count HEAD)
  clojure \
    -X:uberjar Genie.core/process \
    :main-ns Leito.Belle.main \
    :filename "\"out/Leito-Belle.jar\"" \
    :paths '["src"]'
}

release(){
  jar
}

"$@"