package fr.inria.peerunit.exception;

/*
 * Thrown to indicate an peer unit test failure.
 */
public class PeerUnitFailure extends AssertionError {
	private static final long serialVersionUID = 1L;

	/*
	 * Constructs an PeerUnitFailure with no detail message. 
	 */
	public PeerUnitFailure() {
		super ();
	}
	
	/*
	 * Constructs an PeerUnitFailure with its detail message derived from the specified string
	 * @param message - value to be used in constructing detail message
	 */
	public PeerUnitFailure(String message) {
		super (message);
	}

}
