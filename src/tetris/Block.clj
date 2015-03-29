(ns tetris.Block
  (:use clojure.string)
  (:gen-class
   :state state
   :init init
   :prefix "block-"
   :constructors {[clojure.lang.PersistentVector] []}
   :methods [[rows [] long]
             [cols [] long]
             [get [long long] long]
             [rotate [] tetris.Block]]))

(defn block-init [pattern]
  "constructor"
  (if (or (empty? pattern)
          (not= (count pattern) (count (pattern 0))))
    (throw (Exception. "Block pattern must be square"))
    [[] pattern]))

(defn block-rows [this]
  (count (.state this)))

(defn block-cols [this]
  (count ((.state this) 0)))

(defn block-get [this i j]
  (if (and (>= i 0)
           (>= j 0)
           (< i (.rows this))
           (< j (.cols this)))
    (get-in (.state this) [i j])
    (throw (Exception.
              (format
                 "Element index out of bounds: (%d %d) is specified for (%d, %d) size"
                 i j (.rows this) (.cols this))))))

(defn block-rotate [this]
  (let [rows (.rows this)
        cols (.cols this)
        patt (for [i (range rows)
                   j (range cols)
                   :let [el (get-in (.state this) [j (dec (- rows i))])]] el)
        patt (partition cols patt)
        patt (vec (map vec patt))]
    (tetris.Block. patt)))

(defn block-equals [this blk]
  (if (or (not= (.rows this) (.rows blk))
          (not= (.cols this) (.rows blk)))
    false
    (let [rows (.rows this)
          cols (.cols this)
          patt (for [i (range rows)
                     j (range cols)
                     :let [el (= (.get this i j) (.get blk i j))]] el)]
      (every? true? patt))))

(defn block-toString [this]
  (println (class (.state this)))
  (clojure.string/join "" (map (fn [row] (format "%s\n" row)) (.state this))))
