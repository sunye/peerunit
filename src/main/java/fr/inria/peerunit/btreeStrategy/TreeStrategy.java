package fr.inria.peerunit.btreeStrategy;

import fr.inria.peerunit.btree.AbstractBTreeNode;

/**
 * 
 * @author Veronique Pelleau
 * @version 1.0
 * @since 1.0
 */

//The classes that implement a concrete strategy should implement this
//The context class uses this to call the concrete strategy
public interface TreeStrategy {
	public void buildTree();
	public AbstractBTreeNode getNode(Integer i);
	public int getNodesSize();
}
