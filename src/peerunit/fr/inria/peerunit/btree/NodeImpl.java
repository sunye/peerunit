package fr.inria.peerunit.btree;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.btree.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.util.PeerUnitLogger;
import fr.inria.peerunit.util.TesterUtil;

public class NodeImpl  implements Node,Serializable,Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<TreeTesterImpl> testers=new ArrayList<TreeTesterImpl>();	
	
	private List<MethodDescription> testList = new ArrayList<MethodDescription>();
	
	final private Bootstrapper boot;
	
	private ExecutorImpl executor;

	private static PeerUnitLogger log = new PeerUnitLogger(NodeImpl.class
			.getName());
	
	public int id;
	
	private boolean amIRoot=false;
	
	private boolean amILeaf=false;
	
	int numberOfChildren=0;
	
	private TreeElements tree= new TreeElements();
	
	String logFolder = TesterUtil.getLogfolder();
		
	BTreeNode bt;
	
	int childrenTalk=0;
	
	public NodeImpl( Bootstrapper b) throws RemoteException {
		boot=b;
		UnicastRemoteObject.exportObject(this);	
		id=boot.register(this);
		if(id==0)
			amIRoot=true;
		else if (id==Integer.MAX_VALUE) {
			/**
			 * All Nodes are taken
			 */
			System.exit(0);			
		}
		
		System.out.println("Log file to use : "+logFolder+ "/Node" + id + ".log");
		log.createLogger(logFolder+ "/Node" + id + ".log");	
		log.log(Level.INFO, "[NodeImpl] Log file to use : "+logFolder+
				"/Node" + id + ".log");
		log.log(Level.INFO, "[NodeImpl] My Node ID is: "+id);		
	}
	
	public void export(Class<? extends TestCaseImpl> c) {
		
		try {			
			log.log(Level.INFO, "[NodeImpl] Registering actions");			
			executor = new ExecutorImpl();				
			testList=executor.register(c);			
		} catch (SecurityException e) {
			log.logStackTrace(e);			    
		} 
	}
			
	public void run() {
		/**
		 * Now starting the Testers
		 */
		startTesters();			
		if(amIRoot){	
			try {
				Thread.sleep(TesterUtil.getWaitForMethod());
			} catch (InterruptedException e) {			
				log.logStackTrace(e);		
			}						
		}
		log.log(Level.INFO, "[NodeImpl] START EXECUTION ");
		for(MethodDescription md:testList){		
			log.log(Level.INFO, "[NodeImpl] METHOD "+md);
			try {				
				if(amIRoot){
					log.log(Level.INFO, "[NodeImpl] Start action ");
					dispatch(md);
				}else{		
					/**
					 * Wait for parent
					 */
					synchronized(this){
						this.wait();
					}
					if(!amILeaf){
						dispatch(md);
					}else{
						execute(md);						
					}					
					talkToParent();					
				}				
			} catch (InterruptedException e) {				
				log.logStackTrace(e);		
			} 		
		}
		System.exit(0);	
	}
	
	private void dispatch(MethodDescription md) throws InterruptedException {
		log.log(Level.INFO, id+"[NodeImpl] Dispatching action ");		
		talkToChildren();		
		execute(md);		
		/**
		 * Wait for children
		 */
		synchronized(this){
			this.wait();
		}	
	}
	
	private void execute(MethodDescription md){
		log.log(Level.INFO, id+"[NodeImpl]  Executing action "+md);
		for(TreeTesterImpl t:testers){
			t.inbox(md);		
		}					
	}
	
	private void talkToChildren(){		
		for(Node child:tree.getChildren()){
			log.log(Level.INFO, id+"[NodeImpl] talk to kids "+ child);		
			try {
				/**
				 * Talk to children
				 */
				child.send(MessageType.EXECUTE);
			} catch (RemoteException e) {
				log.logStackTrace(e);		
			}
		}
	}
	
	private void talkToParent(){	
		log.log(Level.INFO, id+"[NodeImpl] talk do daddy");	
		try {					
			/**
			 * Talk to parent
			 */
			tree.getParent().send(MessageType.OK);
		} catch (RemoteException e) {
			log.logStackTrace(e);		
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
		/**
		 * Way up 
		 */
		if (message.equals(MessageType.OK)) {	
			log.log(Level.INFO, id+"[NodeImpl]  I finished the execution. Waiting "+
					(numberOfChildren-childrenTalk)+" of my "+numberOfChildren+" children ");
			childrenTalk++;
			
			/**
			 * I have to wait for my children 
			 */			
			if(childrenTalk==numberOfChildren){
				synchronized (this) {
					this.notify();
				}
				childrenTalk=0;
			}
			
			
			/**
			 * now EXECUTE, ERROR and FAIL messages
			 */
		}else {
			/**
			 * Way down
			 */
			if (message.equals(MessageType.EXECUTE)) {				
				log.log(Level.INFO, id+"[NodeImpl]  I'm about to execute.");		
				synchronized (this) {
					this.notify();	
				}						
			}
		}			
	}

	public void setElements(BTreeNode bt,TreeElements tree) throws RemoteException {		
		log.log(Level.INFO, "[NodeImpl] id "+id+" bt "+bt+" tree "+tree);		
		this.tree=tree;
		this.bt=bt;		
		for(BTreeNode child:this.bt.children){
			if(child!=null)
				numberOfChildren++;
		}
		log.log(Level.INFO, "[NodeImpl] I have these number of children: "+numberOfChildren);
		
		amILeaf=bt.isLeaf();
		synchronized (this) {
			this.notify();	
		}		
	}
	
	public int getId(){
		return id;
	}	
	
	public String toString(){
		return "Node id: "+id;
	}
	
	private void startTesters(){
		/**
		 * Initially we wait for the tree construction
		 */
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				log.logStackTrace(e);			    
			}	
		}		
		
		log.log(Level.INFO, "[NodeImpl] Starting "+bt.keys+" Testers ");
		/**
		 * Using bt Node acknowledge the testers it must control, then start them
		 */		
		for(Comparable key:bt.keys){
			if(key != null){
				log.log(Level.INFO, "[NodeImpl] Tester "+key.toString());				
				TreeTesterImpl t=new TreeTesterImpl(new Integer(key.toString()));
				t.setExecutor(executor);
				t.start();				
				testers.add(t);				
			}
		}		
		log.log(Level.INFO, "[NodeImpl] Testers added: "+testers.size());
	}
}
