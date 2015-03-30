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
(def block-margin 3)

;; game parameters
(def time-interval 500)
(def score-table [0 10 30 70 100])

;; game state
(def game-active (atom true))
(def game-score  (atom 0))

(def field (atom (to-array-2d (make-vec-2d field-rows field-cols :value 0))))

;; field colors
(def border-color Color/GRAY)

;; blocks
(def stick [[0 0 0 0]
            [1 1 1 1]
            [0 0 0 0]
            [0 0 0 0]])

(def square [[2 2]
             [2 2]])

(def tblock [[0 0 0]
             [3 3 3]
             [0 3 0]])

(def sblock [[0 4 0]
             [0 4 4]
             [0 0 4]])

(def zblock [[0 5 0]
             [5 5 0]
             [5 0 0]])

(def lblock [[6 6 0]
             [0 6 0]
             [0 6 0]])

(def jblock [[0 7 7]
             [0 7 0]
             [0 7 0]])

(def blocks [stick square tblock sblock zblock lblock jblock])
(def block-colors [Color/WHITE
                   (Color.  64 216 216)   ;; cyan
                   (Color. 216 216  64)   ;; yellow
                   (Color.  64 216  64)   ;; green
                   (Color. 216  64  64)   ;; red
                   (Color.  64  64 216)   ;; blue
                   (Color. 216 128  32)   ;; orange
                   (Color. 216  64 216)]) ;; magenta

(def active-block (atom (Block. (blocks (rand-int (count blocks))))))
(def next-block   (atom (Block. (blocks (rand-int (count blocks))))))

;; get paint color
(defn get-color [id]
  (block-colors id))

;; paint single block
(defn paint-single-block [g color bx by width height]
  (.setColor g color)
  (.fillRect g bx by width height)
  (when (not= color Color/WHITE)
    (.setColor g (.brighter color))
    (.fillRect g bx by block-margin height)
    (.fillRect g bx by width block-margin)
    (.setColor g (.darker color))
    (.fillRect g (+ bx (- cell-size block-margin)) by block-margin height)
    (.fillRect g bx (+ by (- cell-size block-margin)) width block-margin)))

;; paint field
(defn paint-field [g field]
  (.setColor g Color/WHITE)
  (.fillRect g 0 0 (* field-cols cell-size) (* field-rows cell-size))
  ;; paint fixed blocks
  (doseq [i (range field-rows)
          j (range field-cols)]
    (let [bx (* j cell-size)
          by (* i cell-size)
          color (get-color (aget field i j))]
      ;; fill block
      (paint-single-block g color bx by cell-size cell-size)))
  ;; paint active block
  (let [blk-rows (.rows @active-block)
        blk-cols (.cols @active-block)]
    (doseq [i (range blk-rows)
            j (range blk-cols)]
      (let [blk-x (.getX @active-block)
            blk-y (.getY @active-block)
            bx (* (+ blk-x j) cell-size)
            by (* (+ blk-y i) cell-size)
            color (get-color (.get @active-block i j))
            id (.get @active-block i j)]
        (when (not= id 0)
          (paint-single-block g color bx by cell-size cell-size))))))

;; paint next block
(defn paint-next-block [g blk]
  (.setColor g Color/WHITE)
  (.fillRect g 0 0 (* 4 cell-size) (* 4 cell-size))
  (let [rows (.rows blk)
        cols (.cols blk)]
    (doseq [i (range rows)
            j (range cols)]
      (let [bx (* j cell-size)
            by (* i cell-size)
            color (get-color (.get blk i j))]
        (paint-single-block g color bx by cell-size cell-size)))))

;; check the block can be placed for current field
(defn placable? [field block]
  (let [blk-x (.getX block)
        blk-y (.getY block)
        blk-rows (.rows block)
        blk-cols (.cols block)
        flags (for [i (range blk-rows)
                    j (range blk-cols)
                    :let [bx (+ j blk-x)
                          by (+ i blk-y)]] (or (zero? (.get @active-block i j))
                                               (and (>= bx 0)
                                                    (>= by 0)
                                                    (< bx field-cols)
                                                    (< by field-rows)
                                                    (zero? (aget field by bx)))))]
    (every? true? flags)))

;; move down block
;; if impossible return false
(defn move-block [move-func]
  (def copied-block (.copy @active-block))
  (swap! active-block move-func)
  (let [result (placable? @field @active-block)]
    (when (false? result)
      (reset! active-block copied-block))
    result))

;; move down block while it can be moved
(defn move-down-block []
  (while (move-block #(.move % 0 1))))

(defn fix-block []
  (let [blk-x (.getX @active-block)
        blk-y (.getY @active-block)
        blk-rows (.rows @active-block)
        blk-cols (.cols @active-block)]
    (doseq [i (range blk-rows)
            j (range blk-cols)]
      (let [el (.get @active-block i j)]
        (when (not= el 0)
          (aset @field (+ i blk-y) (+ j blk-x) el))))))

(defn line-filled? [line]
  (every? nonzero? line))

;; process filled lines
(defn process-filled-lines []
  (let [unfilled-lines (for [i (range field-rows)
                             :when (not (line-filled? (aget @field i)))] i)
        filled-line-count (- field-rows (count unfilled-lines))]
    ;; update score
    (reset! game-score (+ @game-score (score-table filled-line-count)))
    ;; remove filled lines
    (when (nonzero? filled-line-count)
      (reset! field (to-array-2d (concat (make-vec-2d filled-line-count field-cols)
                                         (for [l unfilled-lines] (aget @field l))))))))

;; proceed game step
(defn proceed-game []
  (if (not (move-block #(.move % 0 1)))
    (do
      (fix-block)
      (process-filled-lines)
      (reset! active-block @next-block)
      (reset! next-block (Block. (blocks (rand-int (count blocks)))))
      (placable? @field @active-block))
    true))

;; next block panel
(defn create-next-panel []
  (proxy [JPanel] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint-next-block g @next-block))
    (getPreferredSize []
      (Dimension. (* 4 cell-size)
                  (* 4 cell-size)))))


;; extends JPanel implements KeyListener
(defn create-game-panel []
  (proxy [JPanel KeyListener] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint-field g @field))
    (keyPressed [e]
      (let [keycode (.getKeyCode e)]
        (do (cond
              (= keycode VK_LEFT)  (move-block #(.move % -1 0))
              (= keycode VK_RIGHT) (move-block #(.move %  1 0))
              (= keycode VK_DOWN)  (move-down-block)
              (= keycode VK_UP)    (move-block #(.move %  0 0))
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
        gamepanel (create-game-panel)
        sidepanel (JPanel.)
        scorelabel (JLabel. "Score: @")
        nextpanel (create-next-panel)]
    (deflayout
      frame (:border)
      {:WEST gamepanel
       :EAST (deflayout (JPanel.) (:border)
               {:NORTH (deflayout sidepanel (:flow :TRAILING)
                         [nextpanel scorelabel])})})
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
          (.repaint nextpanel)
          (.setText scorelabel (format "Score: %d" @game-score))
          (Thread/sleep time-interval)
          (recur))
        (JOptionPane/showMessageDialog frame "Game Over")))))

;; main
(defn -main [& args]
  (game-main))
