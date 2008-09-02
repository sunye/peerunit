package fr.inria.peerunit.btree;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
	
	private Bootstrapper boot;
	
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
		
	private AtomicInteger childrenTalk = new AtomicInteger(0);
	
	private Long time;
		
	MethodDescription mdToExecute;
	
	int treeWaitForMethod=TesterUtil.getTreeWaitForMethod();
	
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
		this.time=System.currentTimeMillis();
		log.log(Level.INFO, "[NodeImpl] START EXECUTION ");
		for(MethodDescription md:testList){		
			mdToExecute=md;
			log.log(Level.INFO, "[NodeImpl] METHOD "+mdToExecute);			
			try {					
				if(amIRoot){
					log.log(Level.INFO, "[NodeImpl] Start action ");
					dispatch();
				}else{		
					/**
					 * Wait for parent
					 */
					synchronized(this){
						this.wait();
					}
					log.log(Level.INFO, "[NodeImpl] I'm about to execute "+md);			
					if(!amILeaf){
						dispatch();
					}else{
						execute();						
					}					
					talkToParent();					
				}				
			} catch (InterruptedException e) {				
				log.logStackTrace(e);		
			} 		
		}
		log.log(Level.INFO, "Whole execution time "+(System.currentTimeMillis()-this.time));
		
		System.exit(0);	
	}
	
	private void dispatch() throws InterruptedException {
		log.log(Level.INFO, id+"[NodeImpl] Dispatching action "+mdToExecute);		
		talkToChildren();		
		execute();		
		/**
		 * Wait for children
		 */
		synchronized(this){
			this.wait();
		}	
	}
	
	private void execute(){
		log.log(Level.INFO, id+"[NodeImpl]  Executing action "+mdToExecute);
		for(TreeTesterImpl t:testers){
			t.inbox(mdToExecute);		
		}					
	}
	
	private void talkToChildren(){		
		for(Node child:tree.getChildren()){
			log.log(Level.INFO, id+"[NodeImpl] talk to kids "+ child);
			log.log(Level.INFO, id+"[NodeImpl] Sending them "+ mdToExecute);		
			try {
				/**
				 * Talk to children
				 */
				child.send(MessageType.EXECUTE,mdToExecute);
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
			Thread.sleep(treeWaitForMethod);
			tree.getParent().send(MessageType.OK,mdToExecute);
		} catch (RemoteException e) {
			log.logStackTrace(e);		
		} catch (InterruptedException e) {
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
	public void send(MessageType message,MethodDescription mdToExecute) throws RemoteException {
		log.log(Level.INFO, id+"[NodeImpl] Daddy asked me to execute "+ mdToExecute);		
		this.mdToExecute=mdToExecute;
		/**
		 * Way up 
		 */
		int talked;
		if (message.equals(MessageType.OK)) {
			talked=childrenTalk.incrementAndGet();
			log.log(Level.INFO, id+"[NodeImpl]  I finished the execution. Waiting "+
					((numberOfChildren-talked)+1)+" of my "+numberOfChildren+" children ");			
			
			/**
			 * I have to wait for my children 
			 */			
			if(talked==numberOfChildren){
				synchronized (this) {
					this.notify();
				}
				childrenTalk.set(0);
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
				TreeTesterImpl t=new TreeTesterImpl(new Integer(key.toString()),boot);
				t.setExecutor(executor);
				t.start();				
				testers.add(t);				
			}
		}		
		log.log(Level.INFO, "[NodeImpl] Testers added: "+testers.size());
	}
}
