package fr.inria.peerunit.rmi.tester;

/**
* @author Eduardo Almeida.
* @version 1.0
* @since 1.0
* @see java.lang.Runnable 
* @see java.lang.Thread
*/
public class Timeout implements Runnable {
		private long timeout;
		private Thread thread;

		public Timeout(Thread t, long millis) {
			timeout = millis;
			thread = t;
		}

		/**
		 * measure the life time of an thread 
		 * 
		 * @param t the tester which will be registered 
		 * @param list the MethodDescription list
		 * @see fr.inria.peerunit.Coordinator#register(fr.inria.peerunit.Tester,fr.inria.peerunit.parser.MethodDescription)
		 * @throws InterruptedException
		 */
		public void run() {
			try {
				thread.join(timeout);
				if (thread.isAlive()) {					
					thread.interrupt();					
				}
			} catch (InterruptedException e) {			
				e.printStackTrace();
			}			
		}		
	
}
