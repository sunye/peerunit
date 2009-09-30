package fr.inria.peerunit;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * 
 * @author Veronique Pelleau
 * @version 1.0
 * @since 1.0
 */
public interface Architecture extends Remote {
	
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
	 * @return A map containing all registered variables for the boostrapper or the coordinator
	 * @throws RemoteException
	 */
	public Map<Integer,Object> getCollection()  throws RemoteException ;
	
	/**
	 * Returns true if the boostrapper or the coordinator contains the specified variable.
	 * @param key Value whose presence is to be tested
	 * @return true if the boostrapper or the coordinator contains the specified variable.
	 * @throws java.rmi.RemoteException
	 */
	public boolean containsKey(Object key) throws RemoteException ;
	
	/**
	 * Clears all global variables for this object
	 * @throws java.rmi.RemoteException
	 */
	public void clearCollection() throws RemoteException ;
}