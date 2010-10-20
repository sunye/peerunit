/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.freepastrytest.model;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sunye
 */
public class Node {

    private final String id;
    private final Set<Node> neighbors;

    protected int index;

    public Node(String str) {
        id = str;
        neighbors = new HashSet<Node>();
    }

    public void updateNodes(Set<Node> set) {
        neighbors.clear();
        neighbors.addAll(set);
    }


    public Set<Node> neighbors() {
        return neighbors;
    }

    @Override
    public String toString() {
        String result;

        result = "Node[" + id + "," + neighbors.size() + "]";

        return result;
    }
}
