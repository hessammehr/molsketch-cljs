# molsketch-cljs

A 2D molecular structure drawing environment in Clojurescript.

![screenshot](screenshot.png)

## Setup

You will need Java and [Leiningen](https://leiningen.org/) installed.
To run molsketch-cljs:

```bash
git clone https://github.com/hessammehr/molsketch-cljs
cd molsketch-cljs
lein figwheel
```

and go `http://localhost:3449`. This gives you a live REPL that you can use
to mess around:

```clojure
(require '[molsketch-cljs.core :as c])
(require '[molsketch-cljs.fragment.xformations :as x])
; Sprout an atom at node #1 (doesn't change the state)
(x/sprout-bond @(:canvas c/app-state) 1)
; Commit the change. The new atom will show up in your browser now.
(reset! (:canvas c/app-state) *1)
```
You can get a nicer REPL (history, line editing) with `rlwrap`:

```bash
rlwrap lein figwheel
```

Pull requests are welcome! Looking to add a feature? I have a few
ideas listed below.

## Todo
* Multiple bonds
* Atom label editing
* Keeping track of implicit hydrogens
* Copy vector graphics to clipboard
* Read/write popular chemical structure formats
* Style support

## License

Copyright © 2015-2017 S. Hessam M. Mehr

MIT License
