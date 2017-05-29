(ns molsketch-cljs.elements)

(def valence {:H 1 :He 0 :Li 1 :Be 2 :B 3 :C 4 :N 3 :O 2 :F 1})

; Key bindings for changing node elements: { `element`: keyword => `key`: char }.
(def keybindings
  {:C \C :N \N :O \O :P \P :S \S})
