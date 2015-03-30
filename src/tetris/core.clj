(ns tetris.core
  (:import (tetris Block)
           (javax.swing JPanel JFrame JOptionPane JButton JLabel)
           (java.awt Color Dimension BorderLayout)
           (java.awt.event KeyListener))
  (:use tetris.utils
        deflayout.core
        clojure.contrib.import-static
        clojure.contrib.swing-utils)
  (:gen-class))

(import-static java.awt.event.KeyEvent VK_LEFT
                                       VK_RIGHT
                                       VK_DOWN
                                       VK_UP
                                       VK_SPACE)

;; field size
(def field-rows 20)
(def field-cols 10)
(def cell-size 20)

;; game parameters
(def time-interval 1000)

;; game state
(def game-active (atom true))

;; field IDs
(def empty-cell 0)
(def filled-cell 1)

(def field (atom (make-vec-2d field-rows field-cols :value empty-cell)))

;; field colors
(def border-color Color/GRAY)

;; blocks
(def stick (Block. [[0 0 0 0]
                    [1 1 1 1]
                    [0 0 0 0]
                    [0 0 0 0]]))

(def square (Block. [[2 2]
                     [2 2]]))

(def tblock (Block. [[0 0 0]
                     [3 3 3]
                     [0 3 0]]))

(def sblock (Block. [[0 4 0]
                     [0 4 4]
                     [0 0 4]]))

(def zblock (Block. [[0 5 0]
                     [5 5 0]
                     [5 0 0]]))

(def lblock (Block. [[6 6 0]
                     [0 6 0]
                     [0 6 0]]))

(def jblock (Block. [[0 7 7]
                     [0 7 0]
                     [0 7 0]]))

(def blocks [stick square tblock sblock zblock lblock jblock])
(def block-colors [Color/WHITE
                   Color/CYAN
                   Color/YELLOW
                   Color/GREEN
                   Color/RED
                   Color/BLUE
                   Color/ORANGE
                   Color/MAGENTA])

(def active-block (atom (blocks (rand-int (count blocks)))))

;; get paint color
(defn get-color [id]
  (block-colors id))

;; paint field
(defn paint-field [g field]
  ;; paint fixed blocks
  (doseq [i (range field-rows)
          j (range field-cols)]
    (let [bx (* j cell-size)
          by (* i cell-size)]
      ;; fill block
      (.setColor g (get-color (get-in @field [i j])))
      (.fillRect g bx by cell-size cell-size)
      ;; fill block border
      (.setColor g border-color)
      (.drawRect g bx by cell-size cell-size)))
  ;; paint active block
  (let [blk-rows (.rows @active-block)
        blk-cols (.cols @active-block)]
    (doseq [i (range blk-rows)
            j (range blk-cols)]
      (let [blk-x (.getX @active-block)
            blk-y (.getY @active-block)
            bx (* (+ blk-x i) cell-size)
            by (* (+ blk-y j) cell-size)
            id (.get @active-block i j)]
        (when (not= id 0)
          (.setColor g (get-color (.get @active-block i j)))
          (.fillRect g bx by cell-size cell-size))))))

;; move down block
;; if impossible return false
(defn move-block [move-func]
  (swap! active-block move-func)
  (let [next-x (.getX @active-block)
        next-y (.getY @active-block)
        blk-rows (.rows @active-block)
        blk-cols (.cols @active-block)
        flags (for [i (range blk-rows)
                    j (range blk-cols)
                    :let [bx (+ j next-x)
                          by (+ i next-y)]] (or (zero? (.get @active-block i j))
                                                (zero? (get-in @field [by bx]))))
        result (every? true? flags)]
    ;;(when result
    ;;  (reset! block-x next-x)
    ;;  (reset! block-y next-y))
    (println "moved")
    result))

(defn fix-block [])

;; proceed game step
(defn proceed-game []
  (when (not (move-block #(.move % 0 1)))
    (fix-block)
    (reset! active-block (blocks (rand-int (count blocks)))))
  true)

;; extends JPanel implements KeyListener
(defn create-game-panel []
  (proxy [JPanel KeyListener] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint-field g field))
    (keyPressed [e]
      (let [keycode (.getKeyCode e)]
        (do (cond
              (= keycode VK_LEFT)  (move-block #(.move % -1 0))
              (= keycode VK_RIGHT) (move-block #(.move % 1 0))
              (= keycode VK_DOWN)  (move-block #(.move % 0 0))
              (= keycode VK_UP)    (move-block #(.move % 0 0))
              (= keycode VK_SPACE) (move-block #(.rotate %))))))
    (getPreferredSize []
      (Dimension. (* field-cols cell-size)
                  (* field-rows cell-size)))
    (keyReleased [e])
    (keyTyped [e])))

;; start game
(defn game-main []
  ;; init game field
  (let [frame (JFrame. "TETRIS")
        gamepanel (create-game-panel)]
    (deflayout
      frame (:border)
      {:WEST gamepanel})
    (doto gamepanel
      (.setFocusable true)
      (.addKeyListener gamepanel)
      (.repaint))
    (doto frame
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.pack)
      (.setVisible true))

    ;; game loop
    (loop []
      (if (proceed-game)
        (do
          (.repaint gamepanel)
          (Thread/sleep time-interval)
          (println "game moving")
          (recur))
        (println "You lose!!")))))

;; main
(defn -main [& args]
  (game-main))
