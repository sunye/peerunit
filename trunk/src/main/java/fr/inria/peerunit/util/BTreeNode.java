package fr.inria.peerunit.util;

/**
 * 
 * @author Veronique Pelleau
 * @version 1.0
 * @since 1.0
 */
public interface BTreeNode {
	
	 public int getId();
	 @SuppressWarnings("unchecked")
	public Comparable[] getKeys();
	 public BTreeNode getParent();
	 public BTreeNode[] getChildren();
	 public boolean isLeaf();
	 public boolean isRoot();
}