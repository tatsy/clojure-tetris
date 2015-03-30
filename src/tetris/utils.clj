(ns tetris.utils)

(defn nonzero? [x]
  (not= x 0))

(defn make-vec-2d [rows cols & {:keys [value] :or {value 0}}]
  (vec (repeat rows (vec (repeat cols value)))))
