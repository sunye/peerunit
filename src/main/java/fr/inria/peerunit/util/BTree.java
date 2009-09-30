/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.util;

/**
 *
 * @author sunye
 */
public interface BTree {

    /**
     * Builds the BTree, based on the number of expectedPeers (Defined by TesterUtil.getExpectedPeers)
     */
    void buildTree();

    /**
     * Returns the node associated to the specified key
     * @param i the key whose associated value is to be returned
     * @return the node to which the specified key is associated
     */
    BTreeNode getNode(Integer i);

    int getNodesSize();

}
