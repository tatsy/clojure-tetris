(ns tetris.Block
  (:use clojure.string)
  (:gen-class
   :name tetris.Block
   :state state
   :init init
   :main false
   :prefix "block-"
   :constructors {[clojure.lang.PersistentVector] []}
   :methods [[rows [] long]
             [cols [] long]
             [getX [] long]
             [getY [] long]
             [get [long long] long]
             [move [long long] tetris.Block]
             [rotate [] tetris.Block]]))

(defn block-init [pattern]
  "constructor"
  (if (or (empty? pattern)
          (not= (count pattern) (count (pattern 0))))
    (throw (Exception. "Block pattern must be square"))
    [[] {:x (atom 1)
         :y (atom 0)
         :rows (count pattern)
         :cols (count (pattern 0))
         :pattern (atom pattern)}]))

(defn block-rows [this]
  ((.state this) :rows))

(defn block-cols [this]
  ((.state this) :cols))

(defn block-getX [this]
  @((.state this) :x))

(defn block-getY [this]
  @((.state this) :y))

(defn block-get [this i j]
  (if (and (>= i 0)
           (>= j 0)
           (< i (.rows this))
           (< j (.cols this)))
    (get-in @((.state this) :pattern) [i j])
    (throw (Exception.
              (format
                 "Element index out of bounds: (%d %d) is specified for (%d, %d) size"
                 i j (.rows this) (.cols this))))))

(defn block-move [this dx dy]
  (let [fields (.state this)
        atom-x (fields :x)
        atom-y (fields :y)]
    (reset! atom-x (+ @atom-x dx))
    (reset! atom-y (+ @atom-y dy))
    this))

(defn block-rotate [this]
  (let [rows (.rows this)
        cols (.cols this)
        patt (for [i (range rows)
                   j (range cols)
                   :let [el (.get this j (dec (- rows i)))]] el)
        patt (partition cols patt)
        patt (vec (map vec patt))]
    (reset! ((.state this) :pattern) patt)
    this))

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
