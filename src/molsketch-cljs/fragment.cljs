; A fragment is the representation of
; a molecular subunit that can be attached
; to another molecule through a root node or bond
; fragments are represented by a hashmap:
; {:nodes [node]
;  :bond [bond]
;  :roots [[:nodes <nodeid>] and/or [:bonds <bondid>]]
;  :graft-dir [x y] (direction the fragment would move if )
; }
(ns molsketch-cljs.fragment
  (:require-macros [com.rpl.specter.macros :refer
                    [transform setval select]])
  (:require [molsketch-cljs.util :as u]
            [com.rpl.specter
             :refer [ALL FIRST LAST VAL MAP-VALS]
             :include-macros true]))

(defn remap [fragment node-mapping bond-mapping]
  (let [lookup {:nodes node-mapping :bonds bond-mapping}]
    (->> fragment
      (transform [:nodes ALL FIRST] node-mapping)
      (transform [:bonds MAP-VALS :nodes ALL] node-mapping)
      (transform [:bonds ALL FIRST] bond-mapping)
      (transform [:roots ALL VAL LAST] #(get-in lookup %1)))))

; Transforms node positions using xform [x y] -> [X Y]
(defn transform [fragment xform]
  (transform [:nodes MAP-VALS :pos] xform fragment))

; Orients fragment so root points in the direction [cx cy]
; (defn rotate [fragment degrees [cx cy]]
;   (transform [:nodes ALL LAST :pos]
;    #(u/rotate % degrees [cx cy]) fragment))
