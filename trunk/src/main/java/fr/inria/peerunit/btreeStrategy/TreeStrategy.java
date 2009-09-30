package fr.inria.peerunit.btreeStrategy;

import java.rmi.RemoteException;

import fr.inria.peerunit.btree.Node;
import fr.inria.peerunit.util.BTreeNode;

/**
 * The classes that implement a concrete strategy should implement this
 * The context class uses this to call the concrete strategy
 * @author Veronique Pelleau
 * @author Jeremy Masson
 * @author Aboubakar Koïta
 *
 */
public interface TreeStrategy {
	public void buildTree();
	public BTreeNode getNode(Object key);  // XXX
	public int getNodesSize();
	public int register(Node node)	throws RemoteException;   
	void setCommunication();
	public int getRegistered();	
	
}
