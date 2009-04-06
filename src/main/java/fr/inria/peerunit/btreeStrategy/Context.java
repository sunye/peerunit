package fr.inria.peerunit.btreeStrategy;

import java.rmi.RemoteException;

import fr.inria.peerunit.btree.Node;
import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;

/**
 * @author Veronique Pelleau
 * @author Aboubakar Koïta
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
    
	public synchronized int register(Node node)	throws RemoteException {
		return strategy.register(node);
	}    
    
	public int getRegistered(){
		return strategy.getRegistered(); 
	}
	
	public void setCommunication(){
		strategy.setCommunication();
	}
	
}
