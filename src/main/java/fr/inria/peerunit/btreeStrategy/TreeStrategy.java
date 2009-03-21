package fr.inria.peerunit.btreeStrategy;

import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;

/**
 * The classes that implement a concrete strategy should implement this
 * The context class uses this to call the concrete strategy
 * @author Veronique Pelleau
 * @author Jeremy Masson
 *
 */
public interface TreeStrategy {
	public void buildTree();
	public AbstractBTreeNode getNode(Integer i);
	public int getNodesSize();
}
