package fr.inria.peerunit;

public enum MessageType {
	/**
	 * OK = way up the tree
	 */
	OK, 
	FAIL, 
	
	/**
	 * EXECUTE is used to get way down the tree
	 */
	EXECUTE,
	ERROR;
}
