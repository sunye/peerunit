/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.freepastrytest.model;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class P2PSystem {

    private static Logger LOG = Logger.getLogger(P2PSystem.class.getName());

    private Map<String, Node> nodes = new HashMap<String, Node>(32);

    public void newNode(String id) {
        nodes.put(id, null);
    }

    public void print() {
        for(String each : nodes.keySet()) {
            LOG.info(each + "\n");
        }

    }
    

}
