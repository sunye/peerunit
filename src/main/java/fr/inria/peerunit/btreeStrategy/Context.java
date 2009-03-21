package fr.inria.peerunit.btreeStrategy;

import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;

/**
 * 
 * @author Veronique Pelleau
 *
 */
public class Context {

    TreeStrategy strategy;
    
    // Constructor
    public Context(TreeStrategy strategy) {
        this.strategy = strategy;
    }
 
    public void buildTree() {
        this.strategy.buildTree();
    }
    
    public AbstractBTreeNode getNode(Integer i) {
        return this.strategy.getNode(i);
    }
    
    public int getNodesSize() {
    	return this.strategy.getNodesSize();
    }

}
