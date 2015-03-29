(ns test-block
  (:use midje.sweet)
  (:import (tetris Block)))

(facts "Constuctor"
  (let [blk (tetris.Block. [[1 2] [3 4]])]
    (fact (.rows blk) => 2)
    (fact (.cols blk) => 2))
  (fact (Block. [1 2 3]) => (throws Exception))
  (fact (Block. [[1 2 3] [4 5 6]]) => (throws Exception)))

(facts "Rotation"
  (let [blk1 (Block. [[1 2] [3 4]])
        blk2 (Block. [[2 4] [1 3]])]
    (fact (= (.rotate blk1) blk2) => true)
    (fact (= (-> blk1 .rotate .rotate .rotate .rotate)
             blk1) => true)))
