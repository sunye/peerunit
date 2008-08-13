package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import fr.inria.peerunit.util.PeerUnitLogger;



public class TreeTesterImpl  implements TreeTester,Serializable,Runnable{
	
	private static final long serialVersionUID = 1L;
	
	public int id;
	
	private TreeElements tree=new TreeElements();
	
	private boolean amIRoot=false;
	
	private boolean amILeaf=true;
	
	private Long time;
	
	private static PeerUnitLogger log = new PeerUnitLogger(TreeTesterImpl.class
			.getName());
	
	private AtomicInteger informedByChildren = new AtomicInteger(0);
	
	final private Bootstrapper boot;
	
	public TreeTesterImpl(Bootstrapper b) throws RemoteException {
		boot = b;
		UnicastRemoteObject.exportObject(this);	
		id=boot.register(this);
		log.log(Level.INFO, "My ID is: "+id);		
	}
	
	public void run(){
		setupTree();
		log.createLogger("tester" + id + ".log");
		int actions=0;		
		if(amIRoot){	
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e1) {			
				e1.printStackTrace();
			}						
		}
		this.time=System.currentTimeMillis();
		while( actions < 8 ){
			try {
				if(amIRoot){
					log.log(Level.INFO, "Start action "+actions);
					dispatch(actions);
				}else{		
					synchronized(this){
						this.wait();
					}
					if(!amILeaf){
						dispatch(actions);
					}else{
						execute(actions);
					}
					talkToParent();
				}				
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
			actions++;
		}
		if(amIRoot)
			log.log(Level.INFO, "Whole execution time "+(System.currentTimeMillis()-this.time));
		else
			log.log(Level.INFO, id+" execution time "+(System.currentTimeMillis()-this.time));
		
		System.exit(0);		
	}
	
	
	/**
	 * Elements of the BTree
	 */	
	public synchronized void setTreeElements(TreeElements tree,boolean isRoot) throws RemoteException{
		this.amIRoot=isRoot;
		this.tree=tree;		
		log.log(Level.INFO, id+"  my parent is "+tree.getParent());
	}
	
	/**
	 * Going way down the tree
	 */
	public synchronized void startExecution() throws RemoteException{
		log.log(Level.INFO, id+" I'm about to execute.");		
		this.notify();				
	}
	
	/**
	 * Going way up the tree
	 */
	public synchronized void endExecution() throws RemoteException{		
		int value= informedByChildren.incrementAndGet();
		log.log(Level.INFO, id+" tester completes. Left "+(tree.getChildren().size()-value)+" children");
		if(value==tree.getChildren().size()){			
			log.log(Level.INFO, id+" will notify thread.");			
			this.notify();			
			log.log(Level.INFO, id+" has notified thread.");
			informedByChildren.set(0);
		}		
	}
	
	public int getId()throws RemoteException {
		return id;
	}

	public void setChildren(TreeTester tester) throws RemoteException {
		tree.add(tester,id);		
		this.amILeaf=false;
	}	
	
	private void setupTree(){
		try {
			while(tree.getParent()==null){
				Thread.sleep(1000);
			}	
			if(!amIRoot)
				tree.getParent().setChildren(this);
			
		} catch (RemoteException e) {				
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void talkToChildren(){
		for(TreeTester t:tree.getChildren()){
			log.log(Level.INFO, id+" talk to kids "+ t);		
			try {
				t.startExecution();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void talkToParent(){	
		log.log(Level.INFO, id+" talk do daddy");	
		try {
			tree.getParent().endExecution();
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}
	
	private void execute(int action){
		log.log(Level.INFO, id+" Executing action"+ action);					
	}
	
	private synchronized void dispatch(int action) throws InterruptedException {
		log.log(Level.INFO, id+" Dispatching action "+action);		
		talkToChildren();
		execute(action);		
		this.wait();		
	}
}
