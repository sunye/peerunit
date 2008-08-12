package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;

import fr.inria.peerunit.util.PeerUnitLogger;
import fr.inria.peerunit.util.TesterUtil;



public class TreeTesterImpl  implements TreeTester,Serializable,Runnable{
	
	private static final long serialVersionUID = 1L;
	
	public int id;
	
	private TreeElements tree=new TreeElements();
	
	private boolean amIRoot=false;
	
	private boolean amILeaf=true;
	
	private Long time;
	
	private static PeerUnitLogger log = new PeerUnitLogger(TreeTesterImpl.class
			.getName());
	
	private int informedByChildren=0;
	
	public static void main(String args[]) throws Exception{		
		TreeTesterImpl tt= new TreeTesterImpl();
		tt.run();
	}
	
	public void run(){
		startNet();
		setupTree();
		log.createLogger("tester" + id + ".log");
		int actions=0;		
		if(amIRoot){	
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e1) {			
				e1.printStackTrace();
			}
			this.time=System.currentTimeMillis();			
		}
		
		while( actions < 8 ){
			try {
				if(amIRoot){
					log.log(Level.INFO, "Start action "+actions);
					dispatch(actions);
				}else{
					synchronized (this) {
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
		
		//System.exit(0);
	}
	
	private  void startNet(){		
		try {
						
			Registry registry = LocateRegistry.getRegistry(TesterUtil.getServerAddr());
			Bootstrapper boot = (Bootstrapper) registry.lookup("Bootstrapper");
			UnicastRemoteObject.exportObject(this);		
			id=boot.register(this);
			log.log(Level.INFO, "My ID is: "+id);		
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} 
	}	
	
	/**
	 * Elements of the BTree
	 */	
	public synchronized void setTreeElements(TreeElements tree,boolean isRoot) throws RemoteException{
		this.amIRoot=isRoot;
		this.tree=tree;		
		log.log(Level.INFO, id+"  my parent is "+tree.getParent());
	}
	
	public void startExecution() throws RemoteException{
		synchronized (this) {
			this.notify();
		}		
	}
	
	/**
	 * Waits all children to inform my parent 
	 */
	public void endExecution() throws RemoteException{
		log.log(Level.INFO, id+" Completes");
		informedByChildren++;
		if(informedByChildren==tree.getChildren().size()){			
			synchronized (this) {
				this.notify();
			}
			informedByChildren=0;
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
			try {
				t.startExecution();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void talkToParent(){	
		log.log(Level.INFO, id+" Talk do daddy");	
		try {
			tree.getParent().endExecution();
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}
	
	private void execute(int action){
		log.log(Level.INFO, id+" Executing action"+ action);					
	}
	
	private void dispatch(int action) throws InterruptedException {
		log.log(Level.INFO, id+" Dispatching action "+action);		
		talkToChildren();
		execute(action);
		synchronized (this) {
			this.wait();
		}
	}
}
