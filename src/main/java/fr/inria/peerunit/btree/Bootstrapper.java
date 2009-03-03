package fr.inria.peerunit.btree;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 */
public interface Bootstrapper extends Remote{
	/**
	 * Adds a new BTree node to this Bootstrapper
	 * 
	 * @param t
	 * @return the generated ID for the added node, or Integer.MAX_VALUE if all nodes have already been registered
	 * @throws java.rmi.RemoteException
	 */
	public int register(Node t) throws RemoteException;	
	
	/**
	 * Used to cache testing global variables
	 * @param key
	 * @param object
	 * @throws RemoteException
	 */
	public void put(Integer key,Object object)  throws RemoteException;

	/**
	 * Used to retrieve testing global variables
	 * @param key
	 * @return Object
	 * @throws RemoteException
	 */
	public Object get(Integer key)  throws RemoteException ;
	
	/**
	 * Used to retrieve all the variables of the testing global variables
	 * @return A map containing all registered variables for this Bootstrapper
	 * @throws RemoteException
	 */
	public Map<Integer,Object> getCollection()  throws RemoteException ;
	
	/**
	 * Returns true if this bootstrapper contains the specified variable.
	 * @param key Value whose presence is to be tested
	 * @return true if this bootstrapper contains the specified variable.
	 * @throws java.rmi.RemoteException
	 */
	public boolean containsKey(Object key) throws RemoteException ;
	
	/**
	 * Clears all global variables for this object
	 * @throws java.rmi.RemoteException
	 */
	public void clearCollection()throws RemoteException ;
}