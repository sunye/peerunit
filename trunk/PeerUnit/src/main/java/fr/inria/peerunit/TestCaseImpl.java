/*
    This file is part of PeerUnit.

    PeerUnit is free software: you can redistribute it and/or modify
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

import fr.inria.peerunit.remote.Tester;


/**
 * An abstract implementation of <tt>TestCase</tt> interface. This class make available
 * in the <i>test case</i> testing global variables who can't be known before runtime.
 * A testing engineer will inherit from this class for define his <i>test case</i>.
 * 
 * @author Eduardo Almeida
 * @author Aboubakar Koïta 
 * @version 1.0
 * @since 1.0
 * @see fr.inria.peerunit.TestCase
 */

public abstract class TestCaseImpl implements TestCase {
	/**
	 * The id of the <i>tester</i> executing the <i>test case</i>
	 */
	private int id;	
	/**
	 * The <i>tester</i> executing the <i>test case</i>
	 */		
	private Tester tester;	

	/**
	 * Set the <i>tester</i> in centralized architecture executing the <i>test</i>
	 * 
	 * @param t the <i>tester</> instance 
	 */
	public void setTester(Tester t) {
		tester = t;
		try {
			id = t.getId();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Return the id of the <i>tester</i> executing the <i>test</i>.
	 * 
	 * @return the id of the <i>tester</i> executing the <i>test</i>
	 */
	public int getId() {
		return id;
	}

	/**
	 * Remote method return the id of the <i>tester</i> executing the <i>test</i>.
	 * 
	 * @return the id of the <i>tester</i> executing the <i>test</i>
	 * @throws RemoteException because the method is distant 
	 */
	@Deprecated
	public int getPeerName() throws RemoteException  {
		return id;
	}

	/**
	 * Allow to <i>test case</i> to stock global variables who will accessed by all others participants
	 * through his <i>tester</i>.
	 * 
	 * @param key the key of <code>object</object>
	 * @param object the variable to stock
	 */			
	public void put(Integer key, Object object) {
		try {
			tester.put(key, object);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Used to retrieve all  peers <keys, value> of the testing global variables.
	 * 
	 * @return all the peers <keys, value> of the testing global variables
	 * @throws RemoteException because the method is distant 
	 */				
	public  Map<Integer,Object> getCollection() throws RemoteException {
		return  tester.getCollection();
	}

	/**
	 * Kill the <i>tester</i> executing the <i>test case</i>
	 */	
	public void kill()  {
		try {
			tester.kill();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Used to retrieve a testing global variable.
	 * 
	 * @param key a key
	 * @return object  a variable corresponding to the key
	 */	
	public Object get(Integer key)  {
		Object result = null;
		try {
			result = tester.get(key);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

    /**
     * Returns <tt>true</tt> if the key <tt>key</tt> can be map in the global variables cache,
     * return <tt>false</tt> else.
     *  
     * @param key a key
     * @return <tt>true</tt> if we can  map the key <tt>key</tt>, return <tt>false</tt> else
	 * @throws RemoteException because the method is distant  
     */		
	public boolean containsKey(Integer key) throws RemoteException {
		return tester.containsKey(key);
	}

	/**
	 * Used to clear the Collection of testing global variables
	 */		
	public void clear() throws RemoteException {
		tester.clear();
	}
}
