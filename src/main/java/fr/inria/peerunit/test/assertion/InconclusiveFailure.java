package fr.inria.peerunit.test.assertion;

import fr.inria.peerunit.exception.PeerUnitFailure;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 *
 */
public class InconclusiveFailure extends PeerUnitFailure {
	
		private static final long serialVersionUID= 1L;

		/**
		 * Constructs an inclusive failure. 
		 *
		 * @since 1.0
		 */
		public InconclusiveFailure() {
			super();
		}
		
		/**
		 * Constructs an inclusive failure. 
		 *
		 * @since 1.0
		 * @param message is the message corresponding to the failure. 
		 */
		public InconclusiveFailure(String message) {
			super(message);
		}
}
