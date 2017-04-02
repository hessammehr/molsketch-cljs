(ns molsketch-cljs.events
  (:require [molsketch-cljs.constants
             :refer [mouse-offset-horizontal
                     mouse-offset-vertical]]))


(defn parse-mouse-event [ev]
  {:x (- (aget ev "pageX") mouse-offset-horizontal)
   :y (- (aget ev "pageY") mouse-offset-vertical)
   :button (aget ev "button")})

(defn parse-keyboard-event [ev]
  {:key (char (aget ev "keyCode"))
   :shift (aget ev "shiftKey")
   :ctrl (aget ev "ctrlKey")})

