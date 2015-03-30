(ns tetris.utils)

(defn make-vec-2d [rows cols & {:keys [value] :or {value 0}}]
  (vec (repeat rows (vec (repeat cols value)))))
