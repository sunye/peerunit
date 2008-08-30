package fr.inria.peerunit.btree;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Level;

import fr.inria.peerunit.StorageTester;
import fr.inria.peerunit.util.PeerUnitLogger;

public class TreeTesterImpl extends Thread implements TreeTester,StorageTester {
	public int id;
	MessageType message;
	boolean executing=true;
	private Inbox inbox;
	private static PeerUnitLogger log = new PeerUnitLogger(TreeTesterImpl.class
			.getName());
	
	public TreeTesterImpl(int id){
		this.id=id;
		log.log(Level.INFO, "[TreeTesterImpl] instance ");
		inbox=new Inbox(log);
	}
	
	public void run() {			
		log.log(Level.INFO, "[TreeTesterImpl] start ");
		while(executing){
			synchronized (this) {			
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}			
			}
			executing=inbox.execute(this.message);
		}
	}

	public void inbox(MessageType message) {		
		this.message=message;
		log.log(Level.INFO, "[TreeTesterImpl] Message "+message);
		this.notify();
	}

	public int getID(){
		return this.id;
	}
	
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public boolean containsKey(Object key) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public Object get(Integer key) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<Integer, Object> getCollection() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void kill() {
		// TODO Auto-generated method stub
		
	}

	public void put(Integer key, Object object) {
		// TODO Auto-generated method stub
		
	}

}
