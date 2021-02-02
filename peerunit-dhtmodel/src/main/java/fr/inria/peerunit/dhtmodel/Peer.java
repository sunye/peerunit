/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.dhtmodel;

import java.util.Set;

/**
 *
 * @author sunye
 */
public interface Peer {

    boolean bootsrap() throws Exception;

    /*
     * DHT Operation.
     */
    String get(String key) throws Exception;

    /*
     * DHT Operation.
     */
    void put(String key, String value) throws Exception;

    /**
     *
     * @return The node Id, as String
     */
    String getId();

    int getPort();

    Set<String> getRoutingTable();

    boolean join() throws Exception;

    void leave();
}
