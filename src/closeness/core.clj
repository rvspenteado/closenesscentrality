(ns closeness.core
  (:require [compojure.core :refer :all]
            [org.httpkit.server :refer [run-server]]))
			
(use 'clojure.data) 
(use 'clojure.set)  
  
(defn exp
  "Exponential function"
  [x n]
  (reduce * (repeat n x)))
  
(defn fk
  "Calculates F(k) for a given k"
  [k]
  (- 1 (exp (/ 1 2) k)))
  
(defn read-lines
  "Read lines from a file"
  [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (doall (line-seq rdr))))

(defn make-edges
  "Create edges from lines read"
  [s]
  (reduce conj (map hash-map [:vertexa :vertexb] (.split s " "))))
  
(defn add-connection
  "Adds connection into graph"
  [{:keys [vertexa vertexb]} final-graph]
  (if (contains? final-graph (keyword vertexa))
    (def graph {(keyword vertexa) (conj ((keyword vertexa) final-graph) (keyword vertexb))})
	(def graph {(keyword vertexa) [(keyword vertexb)]}))
  (if (contains? final-graph (keyword vertexb))
    (conj graph {(keyword vertexb) (conj ((keyword vertexb) final-graph) (keyword vertexa))})
	(conj graph {(keyword vertexb) [(keyword vertexa)]})))
  
(defn create-graph
  "Creates the graph from a given edges list"
  [edges]
  (loop [remaining-edges edges
         final-graph {}]
    (if (empty? remaining-edges)
	  final-graph
	  (let [[edge & remaining] remaining-edges]
	    (recur remaining (into final-graph (add-connection edge final-graph)))))))
  
(defn get-next-nodes
  "Get next depth of nodes from a node list and graph"
  [nodes all-nodes graph]
  (loop [remaining-nodes nodes
        final-nodes []]
	(if (empty? remaining-nodes)
	  (into [] (difference (set final-nodes) (set all-nodes)))
	  (let [[node & remaining] remaining-nodes]
	    (recur remaining (into final-nodes (node graph)))))))

(defn recursive-closeness
  "Recursively gets the closeness from a give root node"
  [remaining-nodes all-nodes farness iterator graph]
	(if (empty? remaining-nodes)
	  (/ 1 farness)
	  (let [next-nodes (get-next-nodes remaining-nodes all-nodes graph)
	        nodes-count (count remaining-nodes)]
	    (recursive-closeness next-nodes (reduce conj all-nodes next-nodes) (+ (* iterator nodes-count) farness) (inc iterator) graph))))
		
(defn add-closeness
  "Adds closeness for a given node and graph"
  [node graph]
  {node (recursive-closeness [node] [node] 0 0 graph)})
  
(defn create-closeness-map
  "Generates the closeness map from a graph"
  [graph-keys graph]
  (loop [remaining-graph-keys graph-keys
         final-map {}]
    (if (empty? remaining-graph-keys)
	  final-map
	  (let [[node & remaining] remaining-graph-keys]
	    (recur remaining (into final-map (add-closeness node graph)))))))
		
(defn update-closeness-k
  "Updates the closeness map with a given k"
  [nodes k closeness-map]
  (let [multiplier (fk k)]
    (loop [remaining-nodes nodes
	       final-map closeness-map]
	  (if (empty? remaining-nodes)
	    final-map
		(let [[node & remaining] remaining-nodes]
		  (recur remaining (into final-map (update-in final-map [node] * multiplier))))))))
			
(defn recursive-fraudulent
  "Set a vertex as fraudulent and update every value from closeness-map"
  [remaining-nodes all-nodes closeness-map graph iterator]
  (if (empty? remaining-nodes)
    closeness-map
	(let [next-nodes (get-next-nodes remaining-nodes all-nodes graph)]
	  (recursive-fraudulent next-nodes (reduce conj all-nodes next-nodes) (update-closeness-k remaining-nodes iterator closeness-map) graph (inc iterator)))))
		 
(defn set-fraudulent
  "Set a vertex as fraudulent and update every value from closeness-map"
  [fraudulent closeness-map graph]
  (recursive-fraudulent [fraudulent] [fraudulent] closeness-map graph 0))
  
  
(def original-edges (map make-edges (read-lines (.getPath (clojure.java.io/resource "edges.txt")))))

(def original-graph (create-graph original-edges))

(def original-closeness-map (create-closeness-map (keys original-graph) original-graph))

(def edges (ref original-edges))

(def closeness-map (ref original-closeness-map))

(defn route-add-edges
  "Route for adding a new edge to the graph"
  [v1 v2]
  (def ed (concat @edges [{:vertexb v1, :vertexa v2}]))
  (def gr (create-graph ed))
  (def cmap (create-closeness-map (keys gr) gr))
  (dosync
    (ref-set edges ed)
    (ref-set closeness-map cmap)
	(str "Added new edge " v1 " <-> " v2)))
	
(defn route-fraudulent
  "Route for setting a vertex as fraudulent"
  [fraudulent]
  (def graph (create-graph @edges))
  (def cmap (set-fraudulent (keyword fraudulent) @closeness-map graph))
  (dosync
    (ref-set closeness-map cmap)
	(str "Node " fraudulent " set as fraudulent.")))

(defroutes myapp
  (GET "/insert/:v1/:v2" [v1 v2] (route-add-edges v1 v2))
  (GET "/set-fraudulent/:fraudulent" [fraudulent] (route-fraudulent fraudulent))
  (GET "/get-map" [] (sort-by val > @closeness-map))
  (GET "/get-edges" [] @edges)
  (GET "/" [] "Rafael Penteado - Closeness Centrality - View README for instructions"))

(defn -main []
  (println "Server Running - Port 5000")
  (run-server myapp {:port 5000}))