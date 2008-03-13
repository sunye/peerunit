package fr.inria.peerunit.exception;

public class PeerUnitFailure extends AssertionError{
	private static final long serialVersionUID = 1L;

	public PeerUnitFailure(String message) {
		super (message);
		
	}
}
