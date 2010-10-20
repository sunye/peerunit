/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.freepastrytest.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class P2PSystem {

    private final static Logger LOG = Logger.getLogger(P2PSystem.class.getName());
    private Map<String, Node> nodes = new HashMap<String, Node>(32);

    public void newNode(String id) {
        nodes.put(id, new Node(id));
    }

    public void nodeUpdate(String str, Set<String> set) {

        //assert nodes.values().containsAll(set);

        Set<Node> neighbors = new HashSet<Node>(set.size());
        Node n = nodes.get(str);
        for (String each : set) {
            if (nodes.containsKey(each)) {
                neighbors.add(nodes.get(each));
            } else {
                LOG.log(Level.WARNING, "Unknwon node id : {0}", each);
            }
        }
        n.updateNodes(neighbors);
    }

    public void print() {
        for (Node each : nodes.values()) {
            LOG.log(Level.INFO, "{0}", each.toString());
        }

    }

    public boolean unicity() {
        LOG.log(Level.INFO, "There are {0} known nodes", nodes.size());
        List<Node> visited = new LinkedList<Node>();
        Collection<Node> coll = nodes.values();
        if (!nodes.isEmpty()) {
            Node head = nodes.values().iterator().next();
            visited.add(head);
            this.visit(visited, head);
        }

        LOG.log(Level.INFO, "Root node could reach {0}  nodes", coll.size());
        return visited.size() == coll.size();
    }


    public boolean distance() {
        // Floyd-Warshall Algorithm Implementation
        
        int size = nodes.size();
        short[][][] distances = new short[size][size][size];
        for(int row = 0; row < size; row++) {
            Arrays.fill(distances[0][row], Short.MAX_VALUE);
        }

        int index = 0;
        for(Node each : nodes.values()) {
            each.index = index++;
        }

        for(Node each : nodes.values()) {
            for(Node neighbor : each.neighbors()) {
                distances[0][each.index][neighbor.index] = 1;
            }
        }

        for(int k = 1; k < size; k++) {
            for(int i = 0; i < size; i++) {
                for(int j = 0; j < size; j++) {
                    distances[k][i][j] = (short) Math.min(distances[k-1][i][j],
                            distances[k-1][i][k] + distances[k-1][k][j]);
                }
            }
        }

        int max = 0;
        for(short row = 0 ; row < size ; row ++) {
            for(short col = 0 ; col < size ; col ++) {
                max = Math.max(max,distances[size-1][row][col]);
            }
        }

        LOG.log(Level.INFO, "Max distance: {0}", max);

        /*
        for(short row = 0 ; row < size ; row ++) {
            StringBuilder out = new StringBuilder(size*2);
            LOG.info(Arrays.toString(distances[size - 1][row]));
        }
         *
         */

        return true;
    }

    // Floyd-Warshall Algorithm
    //    for i = 1 to N
    //      for j = 1 to N
    //          if there is an edge from i to j
    //              dist[0][i][j] = the length of the edge from i to j
    //          else
    //              dist[0][i][j] = INFINITY
    //
    //   for k = 1 to N
    //      for i = 1 to N
    //          for j = 1 to N
    //              dist[k][i][j] = min(dist[k-1][i][j], dist[k-1][i][k] + dist[k-1][k][j])


    private void visit(Collection<Node> visited, Node n) {
        for (Node each : n.neighbors()) {
            if (!visited.contains(each)) {
                visited.add(each);
                this.visit(visited, each);
            }
        }
    }
}
