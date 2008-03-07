/**
 *
 */
package fr.inria.peerunit;

import java.rmi.RemoteException;

/**
 * @author sunye
 *
 * Common interface for all test cases.
 *
 */
public interface TestCase {
	
	public void setId(int i) throws RemoteException;

}
