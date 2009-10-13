/*
    This file is part of PeerUnit.

    Foobar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PeerUnit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	public boolean containsKey(Integer key)throws RemoteException;


	/**
	 * Used to clear the Collection of testing global variables
	 */	
	public void clear() throws RemoteException;

}
