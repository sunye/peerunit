package fr.inria.peerunit.tree;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/** BooStrapper allow to register the tester. It provide a Id for each tester
 * @author Eduardo
 *
 */
public interface Bootstrapper extends Remote{
	
	/** Register the tester
	 * @param t tester Tree
	 * @return return atomic Id
	 * @throws RemoteException
	 */
	public int register(TreeTester t) throws RemoteException;	
	
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

	/** Test if the testing global variable exists 
	 * @param key
	 * @return return true if it exists
	 * @throws RemoteException
	 */
	public boolean containsKey(Object key) throws RemoteException ;

	/** Clear the list
	 * @throws RemoteException
	 */
	public void clearCollection()throws RemoteException ;
}
