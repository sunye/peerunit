package fr.inria.peerunit.btree;

public enum MessageType {
	/**
	 * OK = way up the tree
	 */
	OK, 
	FAIL, 
	
	/**
	 * REGISTER is used to get the tester ID
	 */
	REGISTER,
	
	/**
	 * EXECUTE is used to get way down the tree
	 */
	EXECUTE,
	ERROR;
}
