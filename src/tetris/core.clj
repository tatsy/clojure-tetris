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

(def square (Block. [[1 1]
                     [1 1]]))

(def tblock (Block. [[0 0 0]
                     [1 1 1]
                     [0 1 0]]))

(def sblock (Block. [[0 1 0]
                     [0 1 1]
                     [0 0 1]]))

(def zblock (Block. [[0 1 0]
                     [1 1 0]
                     [1 0 0]]))

(def lblock (Block. [[1 1 0]
                     [0 1 0]
                     [0 1 0]]))

(def jblock (Block. [[0 1 1]
                     [0 1 0]
                     [0 1 0]]))

(def block-x (atom 0))
(def block-y (atom 0))
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
  (cond
    (= id empty-cell) (Color/WHITE)
    (= id filled-cell) (Color/RED)))

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
      (let [bx (* (+ @block-x i) cell-size)
            by (* (+ @block-y j) cell-size)
            id (.get @active-block i j)]
        (when (not= id 0)
          (.setColor g (get-color (.get @active-block i j)))
          (.fillRect g bx by cell-size cell-size))))))

;; move down block
;; if impossible return false
(defn move-down-block []
  true)

(defn fix-block [])

;; proceed game step
(defn proceed-game []
  (when (not (move-down-block))
    (fix-block)
    (reset! active-block (blocks (rand-int (count blocks))))))

;; extends JPanel implements KeyListener
(defn create-game-panel []
  (proxy [JPanel KeyListener] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint-field g field))
    (keyPressed [e]
      (let [keycode (.getKeyCode e)]
        (do (cond
              (= keycode VK_LEFT) (println "Left")
              (= keycode VK_RIGHT) (println "Right")
              (= keycode VK_DOWN) (println "Down")
              (= keycode VK_UP) (println "Up")
              (= keycode VK_SPACE) (println "Space")))))
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
