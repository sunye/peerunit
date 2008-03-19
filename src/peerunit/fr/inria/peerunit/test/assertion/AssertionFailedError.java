package fr.inria.peerunit.test.assertion;

import fr.inria.peerunit.exception.PeerUnitFailure;

public class AssertionFailedError extends PeerUnitFailure{
	private static final long serialVersionUID= 1L;

	public AssertionFailedError() {
	}

	public AssertionFailedError(String message) {
		super(message);
	}
}
