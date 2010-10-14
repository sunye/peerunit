/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.freepastrytest.model;

import java.util.ArrayList;
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

        assert nodes.values().containsAll(set); 

        Set<Node> neighbors = new HashSet<Node>(set.size());
        Node n = nodes.get(str);
        for(String each : set) {
            neighbors.add(nodes.get(each));
        }
        n.updateNodes(neighbors);
    }

    public void print() {
        for(Node each : nodes.values()) {
            LOG.log(Level.INFO, "{0}\n", each.toString());
        }

    }

    public boolean unicity() {
        List<Node> visited = new LinkedList<Node>();
        Collection<Node> coll = nodes.values();
        if (!nodes.isEmpty()) {
           Node head = nodes.values().iterator().next();
           visited.add(head);
           this.visit(visited, head);
       }

       return visited.size() == coll.size();
    }

    private void visit(Collection<Node> visited, Node n) {
        for(Node each : n.neighbors()) {
            if(!visited.contains(each)){
                visited.add(each);
                this.visit(visited, each);
            }
        }
    }

}
