(ns tetris.core
  (:import (tetris Block)))

(defn -main [& args]
  (let [blk (Block. [[1 2] [3 4]])
        rot (Block. [[2 4] [1 3]])]
    (if (= (.rotate blk) rot)
      (println "YES")
      (println "NO"))))
