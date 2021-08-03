#!/bin/bash

repl(){
  clj \
    -X:repl deps-repl.core/process \
    :main-ns ged.main \
    :port 7788 \
    :host '"0.0.0.0"' \
    :repl? true \
    :nrepl? false
}

main(){
  clojure \
    -J-Dclojure.core.async.pool-size=1 \
    -J-Dclojure.compiler.direct-linking=false \
    -M -m ged.main
}

uberjar(){
  clj \
    -X:uberjar genie.core/process \
    :uberjar-name out/ged.standalone.jar \
    :main-ns ged.main
  mkdir -p out/jpackage-input
  mv out/ged.standalone.jar out/jpackage-input/
}

"$@"
