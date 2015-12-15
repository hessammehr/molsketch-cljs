(ns molsketch-cljs.actions
  (:require [molsketch-cljs.core :refer [app-state]]
            [molsketch-cljs.functional :refer [sprout-bond]]))

(defn sprout-from-hovered-or-selected []
  (if-let [[_ n-id] (get-in @app-state [:status :hovered])]
    (swap! app-state sprout-bond n-id)
    (when-let [[_ n-id] (get-in @app-state [:status :selected])]
      (swap! app-state sprout-bond n-id))))
