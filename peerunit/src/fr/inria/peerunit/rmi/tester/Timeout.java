package fr.inria.peerunit.rmi.tester;

public class Timeout implements Runnable {
		private long timeout;
		private Thread thread;

		public Timeout(Thread t, long millis) {
			//assert t != null;
			//assert thread != null;

			timeout = millis;
			thread = t;
		}

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
