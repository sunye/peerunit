package fr.inria.peerunit.btree;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import fr.inria.peerunit.util.PeerUnitLogger;

public class NodeImpl  implements Node,Serializable,Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<TreeTester> testers=new ArrayList<TreeTester>();	
	
	final private Bootstrapper boot;

	private static PeerUnitLogger log = new PeerUnitLogger(NodeImpl.class
			.getName());
	
	public int id;
	
	private boolean amIRoot=false;
	
	private TreeElements tree= new TreeElements();
	
	public NodeImpl( Bootstrapper b) throws RemoteException {
		boot=b;
		UnicastRemoteObject.exportObject(this);	
		id=boot.register(this);
		if(id==0)
			amIRoot=true;
	}
			
	public void run() {
		while(true){
			
		}		
	}

	/**
	 * Receive a message from another Node.
	 * OK are sent only to Nodes (way up the tree)
	 * EXECUTE are sent to both Testers and Nodes (way down the tree)
 	 * FAIL and ERROR are sent only to Testers
 	 * REGISTER used by Testers to get their ID and by Nodes to store their Testers
	 * @param t
	 * @param message
	 * @throws RemoteException
	 */
	public void send(MessageType message) throws RemoteException {
		if (message.equals(MessageType.OK)) {		
			// way up
			
			/**
			 * now EXECUTE, ERROR and FAIL messages
			 */
		}else {
			for(TreeTester t:testers){
				t.inbox(message);		
			}	
			if (message.equals(MessageType.EXECUTE)) {			
				// way down
			}
		}			
	}

	public void setElements(BTreeNode bt,TreeElements tree) throws RemoteException {		
		/**
		 * Using bt Node acknowledge the testers it must control, then start them
		 */
		for(Comparable key:bt.keys){
			if(key != null){				
				TreeTester t=new TreeTesterImpl(new Integer(key.toString()));
				t.run();
				testers.add(t);	
			}
		}
				
		/**
		 * Children are set. Now Node can talk to them to set parents.
		 */
		this.tree=tree;
		for(Node node:tree.getChildren()){
			node.setParent(this);			
		}
		
	}
	public void setParent(Node parent)throws RemoteException {
		tree.setParent(parent);
	}
}
