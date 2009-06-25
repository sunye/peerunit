package fr.inria.peerunit.btree;

import java.rmi.RemoteException;

import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;


public interface TreeTester extends Tester {	
	/**
	 * Sends a method to this tester to be processed by
	 * it's test class
	 * @param md
	 */
	public void inbox(MethodDescription md) throws RemoteException;	
}
