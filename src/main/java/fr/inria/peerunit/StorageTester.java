package fr.inria.peerunit;

import java.rmi.RemoteException;
import java.util.Map;
/**
 * This interface contains some methods for the stocking and the retrieval of
 * testing global variables who can't be known before runtime.
 * 
 * @author Eduardo Almeida
 * @author Aboubakar Koïta 
 * @version 1.0
 * @since 1.0
 * @see fr.inria.peerunit.VolatileTester
 * @see fr.inria.peerunit.rmi.tester.TesterImpl
 * @see Tester
 */

public interface StorageTester  {
	
	/**
	 * Allow to stock global variables who will accessed by all others
	 * participants.
	 * 
	 * @param key
	 *            the key of <code>object</object>
	 * @param object
	 *            the variable to stock
	 */	
	public void put(Integer key,Object object) throws RemoteException ;
	

	/**
	 * Used to retrieve all the peer <keys, value> of the testing global
	 * variables.
	 * 
	 * @return all the peer <keys, value> of the testing global variables
	 * @throws RemoteException
	 *             because the method is distant
	 */
	public  Map<Integer,Object> getCollection() throws RemoteException;

	/**
	 * Used to retrieve a testing global variable.
	 * 
	 * @param key
	 *            a key
	 * @return object a variable corresponding to the key
	 */
	public Object get(Integer key) throws RemoteException ;
	
	/**
	 * Returns <tt>true</tt> if the key <tt>key</tt> can be map in the global
	 * variables cache, return <tt>false</tt> else.
	 * 
	 * @param key
	 *            a key
	 * @return <tt>true</tt> if we can map the key <tt>key</tt>, return
	 *         <tt>false</tt> else
	 * @throws RemoteException
	 *             because the method is distant
	 */	
	public boolean containsKey(Object key)throws RemoteException;


	/**
	 * Used to clear the Collection of testing global variables
	 */	
	public void clear() throws RemoteException;

}
