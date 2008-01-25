package fr.inria.peerunit.exception;

public class PasspartoutFailure extends AssertionError{
	private static final long serialVersionUID = 1L;

	public PasspartoutFailure(String message) {
		super (message);
		
	}
}
