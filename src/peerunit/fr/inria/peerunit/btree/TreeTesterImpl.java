package fr.inria.peerunit.btree;

import java.util.logging.Level;

import fr.inria.peerunit.util.PeerUnitLogger;

public class TreeTesterImpl extends Thread implements TreeTester {
	public int id;
	MessageType message;
	boolean executing=true;
	private Inbox inbox=new Inbox();
	private static PeerUnitLogger log = new PeerUnitLogger(TreeTesterImpl.class
			.getName());
	
	public TreeTesterImpl(int id){
		this.id=id;
		log.log(Level.INFO, "instance ");	
	}
	
	public void run() {			
		log.log(Level.INFO, "start ");
		/*while(executing){
			synchronized (this) {			
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}			
			}
			inbox.execute(message);
		}*/
	}

	public void inbox(MessageType message) {		
		this.message=message;
		log.log(Level.INFO, "Message "+message);
		this.notify();
	}

}
