package fr.inria.peerunit.btreeStrategy;

import fr.inria.peerunit.btree.BTree;
import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;
import fr.inria.peerunit.util.TesterUtil;

public class ConcreteBtreeStrategy implements TreeStrategy {

	private BTree btree;
	
	public ConcreteBtreeStrategy() {
		btree = new BTree(TesterUtil.getTreeOrder());
	}
	
	public void buildTree() {
		btree.buildTree();
	}

	public AbstractBTreeNode getNode(Integer i) {
		return btree.getNode(i);
	}

	public int getNodesSize() {
		return btree.nodes.size();
	}

}
