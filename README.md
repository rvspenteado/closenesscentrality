# closeness

A Clojure library designed to calculate the closeness centrality.

## Usage

Run the program:
$ lein run -m closeness.core

A new local server will be started at port 5000.
The program is loaded with the resource file edges.txt, generating the initial closeness map.

http://localhost:5000/ - Main Page
http://localhost:5000/get-edges - Returns all the edges from the social network
http://localhost:5000/get-map - Returns a map with the closeness centrality for each of the nodes
http://localhost:5000/insert/[NODE1]/[NODE2] - Insert a new edge between NODE1 and NODE2 (e.g. http://localhost:5000/insert/49/4)
http://localhost:5000/set-fraudulent/[FRAUDULENT] - Sets a given node as fraudulent (e.g. http://localhost:5000/set-fraudulent/49)

After inserting a new edge or setting a node as fraudulent, the closeness map is updated. Please use http://localhost:5000/get-map to see it.

## License

Copyright © 2016 Rafael Penteado

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
