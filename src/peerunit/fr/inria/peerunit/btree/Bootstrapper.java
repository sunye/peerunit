package fr.inria.peerunit.btree;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Bootstrapper extends Remote{
	public int register(Node t) throws RemoteException;	
	/**
	 * Used to cache testing global variables
	 * @param key
	 * @param object
	 * @throws RemoteException
	 */
	public void put(Integer key,Object object)  throws RemoteException;

	/**
	 *  Used to retrieve testing global variables
	 * @param key
	 * @return Object
	 * @throws RemoteException
	 */
	public Object get(Integer key)  throws RemoteException ;
	/**
	 * Used to retrieve all the keys of the testing global variables
	 * @param key
	 * @return Object
	 * @throws RemoteException
	 */
	public  Map<Integer,Object> getCollection()  throws RemoteException ;

	public boolean containsKey(Object key) throws RemoteException ;

	public void clearCollection()throws RemoteException ;
}