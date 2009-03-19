package fr.inria.peerunit.btreeStrategy;


import fr.inria.peerunit.btree.AbstractBTreeNode;

/**
 * 
 * @author Veronique Pelleau
 * @version 1.0
 * @since 1.0
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
