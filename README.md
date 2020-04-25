# molsketch-cljs

A 2D molecular structure drawing environment in Clojurescript.

![screenshot](screenshot.png)

## Setup

You will need Java and [Leiningen](https://leiningen.org/) installed.
To run molsketch-cljs:

```bash
git clone https://github.com/hessammehr/molsketch-cljs
cd molsketch-cljs
lein fig:build
```

This will automatically launch a browser tab at `http://localhost:9500`. 

## Keybindings
While hovering on a bond:
- **del**: Delete bond
- **1, 2, 3**: Make bond single, double, or triple

While hovering on an atom:
- **0**: Sprout a new atom
- **3, 4, 5, 6**: Graft a cyclopropyl, cyclobutyl, cyclopentyl, or cyclohexyl
ring at atom.
- **C, N, O, S, P**: Change atom element to C, N, etc.

## REPL fun
Figwheel gives you a live REPL that you can use to mess around:

```clojure
(require '[molsketch-cljs.core :as c])
(require '[molsketch-cljs.fragment.xformations :as x])
; Sprout an atom at node #1 (doesn't change the state)
(x/sprout-bond @(:canvas c/app-state) 1)
; Commit the change. The new atom will show up in your browser now.
(reset! (:canvas c/app-state) *1)
```

Pull requests are welcome! I have a few ideas listed below.

## Todo
* [X] Multiple bonds
* [ ] Custom atom labels
* [ ] Keeping track of implicit hydrogens
* [ ] Copy vector graphics to clipboard
* [ ] Read/write popular chemical structure formats
* [ ] Style support

## License

Copyright © 2015-2020 S. Hessam M. Mehr

MIT License
