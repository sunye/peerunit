package fr.inria.peerunit;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author Veronique Pelleau
 * @version 1.0
 * @since 1.0
 */
public abstract class ArchitectureImpl implements Architecture {
	/**
	 * Caching global variables
	 */
	private Map<Integer, Object> cacheMap = new ConcurrentHashMap<Integer, Object>();
		
	/**
	 * Clears all global variables for this object
	 * @throws java.rmi.RemoteException
	 */
	public void clearCollection() throws RemoteException {
		cacheMap.clear();
	}

	/**
	 * Returns true if this bootstrapper contains the specified variable.
	 * @param key Value whose presence is to be tested
	 * @return true if this bootstrapper contains the specified variable.
	 * @throws java.rmi.RemoteException
	 */
	public boolean containsKey(Object key) throws RemoteException {
		return cacheMap.containsKey(key);
	}

	/**
	 * Used to retrieve testing global variables
	 * @param key
	 * @return Object
	 * @throws RemoteException
	 */
	public Object get(Integer key) throws RemoteException {
		return cacheMap.get(key);
	}

	/**
	 * Used to retrieve all the variables of the testing global variables
	 * @return A map containing all registered variables for this Bootstrapper
	 * @throws RemoteException
	 */
	public Map<Integer, Object> getCollection() throws RemoteException {
		return cacheMap;
	}

	/**
	 * Used to cache testing global variables
	 * @param key
	 * @param object
	 * @throws RemoteException
	 */
	public void put(Integer key, Object object) throws RemoteException {
		cacheMap.put(key, object);
	}

}
