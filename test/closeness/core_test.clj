(ns closeness.core-test
  (:require [clojure.test :refer :all]
            [closeness.core :refer :all]))
	
(def edges-test (map make-edges (read-lines (.getPath (clojure.java.io/resource "edges-test.txt")))))

(def graph-test (create-graph edges-test))

(def closeness-map-test (create-closeness-map (keys graph-test) graph-test))
	
(deftest fk-test
  (testing "F(k) for given k's"
    (is (= 0 (fk 0)))
	(is (= 1/2 (fk 1)))
	(is (= 3/4 (fk 2)))
	(is (= 7/8 (fk 3)))
	(is (= 15/16 (fk 4)))))
	
(deftest exp-test
  (testing "Exp x^y"
    (is (= 0 (exp 0 2)))
	(is (= 1 (exp 1 0)))
	(is (= 1 (exp 2 0)))
	(is (= 16 (exp 2 4)))
	(is (= 243 (exp 3 5)))))
	
(deftest file-to-edges-test
  (testing "Tests if the file is correctly read and the edges are created"
    (is (= '({:vertexb "7", :vertexa "1"} {:vertexb "2", :vertexa "7"} {:vertexb "3", :vertexa "1"} {:vertexb "4", :vertexa "1"} {:vertexb "2", :vertexa "1"} {:vertexb "8", :vertexa "4"} {:vertexb "10", :vertexa "8"} {:vertexb "6", :vertexa "3"} {:vertexb "10", :vertexa "6"} {:vertexb "5", :vertexa "3"} {:vertexb "5", :vertexa "2"}) edges-test))))
	
(deftest add-connection-test
  (testing "Adds a new connection to new nodes"
    (is (= {:4 [:5], :5 [:4]} (add-connection {:vertexa "4", :vertexb "5"} {:2 [:3], :3 [:2]}))))
  (testing "Adds a new connection to one existing node"
    (is (= {:4 [:2], :2 [:3 :4]} (add-connection {:vertexa "4", :vertexb "2"} {:2 [:3], :3 [:2]}))))
  (testing "Adds a new connection to both existing nodes"
    (is (= {:3 [:2 :4], :4 [:2 :3]} (add-connection {:vertexa "3", :vertexb "4"} {:2 [:3 :4], :3 [:2], :4 [:2]})))))
	
(deftest create-graph-test
  (testing "Graph map creation test with file sample"
    (is (= {:10 [:8 :6], :4 [:1 :8], :7 [:1 :2], :1 [:7 :3 :4 :2], :8 [:4 :10], :2 [:7 :1 :5], :5 [:3 :2], :3 [:1 :6 :5], :6 [:3 :10]} (create-graph edges-test))))
  (testing "Graph map creation test with basic edges list"
    (is (= {:7 [:2], :2 [:7 :1], :1 [:3 :4 :2], :3 [:1], :4 [:1]} (create-graph '({:vertexb "2", :vertexa "7"} {:vertexb "3", :vertexa "1"} {:vertexb "4", :vertexa "1"} {:vertexb "1", :vertexa "2"})))))
  (testing "Graph map creation test with empty edges list"
    (is (= {} (create-graph '())))))
	
(deftest get-next-nodes-test
  (testing "Get the next nodes from node 2"
    (is (= [:7 :1 :5] (get-next-nodes [:2] [:2] graph-test))))
  (testing "Get the next nodes from nodes 7 1 5 without all-nodes reference"
    (is (= [:4 :7 :1 :2 :3] (get-next-nodes [:7 :1 :5] [] graph-test))))
  (testing "Get the next nodes from nodes 7 1 5"
    (is (= [:4 :2 :3] (get-next-nodes [:7 :1 :5] [:7 :1 :5] graph-test))))
  (testing "Get the next nodes from empty nodes list"
    (is (= [] (get-next-nodes [] [:7 :1 :5] graph-test)))))
	
(deftest add-closeness-test
  (testing "Get the closeness for node 1, from sample graph"
    (is (= {:1 1/13} (add-closeness :1 graph-test))))
  (testing "Get the closeness for node 2, from sample graph"
    (is (= {:2 1/17} (add-closeness :2 graph-test))))
  (testing "Get the closeness for node 3, from sample graph"
    (is (= {:3 1/14} (add-closeness :3 graph-test))))
  (testing "Get the closeness for node 4, from sample graph"
    (is (= {:4 1/16} (add-closeness :4 graph-test))))
  (testing "Get the closeness for node 5, from sample graph"
    (is (= {:5 1/18} (add-closeness :5 graph-test))))
  (testing "Get the closeness for node 6, from sample graph"
    (is (= {:6 1/17} (add-closeness :6 graph-test))))
  (testing "Get the closeness for node 7, from sample graph"
    (is (= {:7 1/18} (add-closeness :7 graph-test))))
  (testing "Get the closeness for node 8, from sample graph"
    (is (= {:8 1/19} (add-closeness :8 graph-test))))
  (testing "Get the closeness for node 10, from sample graph"
    (is (= {:10 1/20} (add-closeness :10 graph-test))))
  (testing "Get the closeness for unexisting node from sample graph"
    (is (= {} (add-closeness :9 graph-test)))))
	
(deftest create-closeness-map-test
  (testing "Get the full closeness map from sample graph"
    (is (= {:10 1/20, :4 1/16, :7 1/18, :1 1/13, :8 1/19, :2 1/17, :5 1/18, :3 1/14, :6 1/17} (create-closeness-map (keys graph-test) graph-test))))
  (testing "Get a partial closeness map from sample graph"
    (is (= {:10 1/20, :4 1/16, :7 1/18} (create-closeness-map '(:10 :4 :7) graph-test))))
  (testing "Get a partial closeness map from empty graph"
    (is (= {} (create-closeness-map '(:10 :4 :7) {}))))
  (testing "Get a empty closeness map from sample graph"
    (is (= {} (create-closeness-map '() graph-test))))
  (testing "Get the closeness map from empty graph"
    (is (= {} (create-closeness-map '() {})))))

(deftest set-fraudulent-test
  (testing "Sets node :2 as fraudulent on sample graph"
    (is (= {:10 3/64, :4 3/64, :7 1/36, :1 1/26, :8 7/152, :2 0N, :5 1/36, :3 3/56, :6 7/136} (set-fraudulent :2 closeness-map-test graph-test))))
  (testing "Sets node :3 as fraudulent on sample graph"
    (is (= {:10 3/80, :4 3/64, :7 1/24, :1 1/26, :8 7/152, :2 3/68, :5 1/36, :3 0N, :6 1/34} (set-fraudulent :3 closeness-map-test graph-test))))
  (testing "Sets node :4 as fraudulent on sample graph"
    (is (= {:10 3/80, :4 0N, :7 1/24, :1 1/26, :8 1/38, :2 3/68, :5 7/144, :3 3/56, :6 7/136} (set-fraudulent :4 closeness-map-test graph-test))))
  (testing "Sets node :5 as fraudulent on sample graph"
    (is (= {:10 7/160, :4 7/128, :7 1/24, :1 3/52, :8 15/304, :2 1/34, :5 0N, :3 1/28, :6 3/68} (set-fraudulent :5 closeness-map-test graph-test))))
  (testing "Sets node :6 as fraudulent on sample graph"
    (is (= {:10 1/40, :4 7/128, :7 7/144, :1 3/52, :8 3/76, :2 7/136, :5 1/24, :3 1/28, :6 0N} (set-fraudulent :6 closeness-map-test graph-test))))
  (testing "Sets unexisting as fraudulent on sample graph"
    (is (= closeness-map-test (set-fraudulent :9 closeness-map-test graph-test))))
  (testing "Sets node :2 as fraudulent on empty graph"
    (is (= closeness-map-test (set-fraudulent :2 closeness-map-test {}))))
  (testing "Sets node :2 as fraudulent on sample graph with empty closeness map"
    (is (= {} (set-fraudulent :2 {} graph-test)))))
