package fr.inria.peerunit.btree;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author Eduardo Almeida, Veronique Pelleau
 * @version 1.0
 * @since 1.0
 */
public interface Bootstrapper extends Remote {
	/**
	 * Adds a new BTree node to this Bootstrapper
	 * 
	 * @param t
	 * @return the generated ID for the added node, or Integer.MAX_VALUE if all nodes have already been registered
	 * @throws java.rmi.RemoteException
	 */
	public int register(Node t) throws RemoteException;	
	
	
	/**
	 * Return true if id follow to Bootstrapper
	 * @param id
	 * @return
	 * @throws RemoteException
	 */
	public boolean isRoot(int id) throws RemoteException;

}