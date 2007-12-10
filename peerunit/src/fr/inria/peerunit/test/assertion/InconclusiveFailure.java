package fr.inria.peerunit.test.assertion;

public class InconclusiveFailure 
	extends AssertionError{
		private static final long serialVersionUID= 1L;

		public InconclusiveFailure() {
		}

		public InconclusiveFailure(String message) {
			super(message);
		}
}
