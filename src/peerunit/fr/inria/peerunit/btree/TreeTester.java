package fr.inria.peerunit.btree;

import fr.inria.peerunit.parser.MethodDescription;


public interface TreeTester {	
	/**
	 * Sends a method to this tester to be processed by
	 * it's test class
	 * @param md
	 */
	public void inbox(MethodDescription md);	
}
