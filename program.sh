#!/bin/bash

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

main(){

  server

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
