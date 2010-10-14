/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.freepastrytest.model;

import java.util.Set;

/**
 *
 * @author sunye
 */
public class NodeUpdate {

    private final String id;
    private final Set<String> neighbors;

    public NodeUpdate(String str, Set<String> set) {
        id = str;
        neighbors = set;
    }


    public String id() {
        return id;
    }

    public Set<String> neighbors() {
        return neighbors;
    }
}
