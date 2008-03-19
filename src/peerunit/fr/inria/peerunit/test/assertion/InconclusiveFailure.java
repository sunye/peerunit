package fr.inria.peerunit.test.assertion;

import fr.inria.peerunit.exception.PeerUnitFailure;

public class InconclusiveFailure 
	extends PeerUnitFailure{
		private static final long serialVersionUID= 1L;

		public InconclusiveFailure() {
			super();
		}
		
		public InconclusiveFailure(String message) {
			super(message);
		}
}
