/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.util;

/**
 *
 * @author sunye
 */
public interface HNode<K,V> {

    /**
     *
     * @return the value stored in this HNode.
     */
    V value();

    /**
     *
     * @return the key stored in this HNode.
     */
    K key();

    /**
     * 
     * @return The children of this node.
     */
    HNode<K,V>[] children();

    boolean isLeaf();
}
