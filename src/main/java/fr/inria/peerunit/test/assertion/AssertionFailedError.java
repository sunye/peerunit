package fr.inria.peerunit.test.assertion;

import fr.inria.peerunit.exception.PeerUnitFailure;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 * 
 * @see PeerUnitFailure
 *
 */
public class AssertionFailedError extends PeerUnitFailure {
	
	private static final long serialVersionUID= 1L;

	/**
	 * Constructs a assertion failed error.
	 * 
	 *
	 * @since 1.0
	 */
	public AssertionFailedError(){}

	/**
	 * Constructs a assertion failed error.
	 * 
	 *
	 * @since 1.0
	 * @param message is the message corresponding to the error. 
	 */
	public AssertionFailedError(String message) {
		super(message);
	}
}
