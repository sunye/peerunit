package fr.inria.peerunit.btreeStrategy;

/**
 * 
 * @author Veronique Pelleau
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractBTreeNode {
	
	abstract public int getId();
	abstract public Comparable[] getKeys();
	abstract public AbstractBTreeNode getParent();
	abstract public AbstractBTreeNode[] getChildren();
	abstract public boolean isLeaf();
	abstract public boolean isRoot();
}
