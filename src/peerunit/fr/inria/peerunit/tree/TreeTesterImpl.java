package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;



public class TreeTesterImpl  implements TreeTester,Serializable,Runnable{
	
	private static final long serialVersionUID = 1L;
	
	public int id;
	
	private TreeElements tree=new TreeElements();
	
	private boolean amIRoot=false;
	
	private boolean amILeaf=true;
	
	private Long time;

	public static void main(String args[]) throws Exception{
		TreeTesterImpl tt= new TreeTesterImpl();
		tt.run();
	}
	
	public void run(){
		startNet();
		setupTree();
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
					System.out.println("Start action "+actions);
					talkToChildren();
					execute();
					synchronized (this) {
						this.wait();
					}					
				}else{
					synchronized (this) {
						this.wait();
					}
					if(!amILeaf){
						talkToChildren();
						execute();
						synchronized (this) {
							this.wait();
						}
					}else{
						execute();
					}
					talkToParent();
				}				
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
			actions++;
		}
		if(amIRoot)
			System.out.println("Execution time "+(System.currentTimeMillis()-this.time));
		
		System.exit(0);
	}
	
	private  void startNet(){		
		try {
						
			Registry registry = LocateRegistry.getRegistry("172.16.9.101");
			Bootstrapper boot = (Bootstrapper) registry.lookup("Bootstrapper");
			UnicastRemoteObject.exportObject(this);		
			id=boot.register(this);
			System.out.println("My ID is: "+id);		
			
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
	}
	
	public void startExecution() throws RemoteException{
		synchronized (this) {
			this.notifyAll();
		}		
	}

	public void endExecution() throws RemoteException{
		synchronized (this) {
			this.notifyAll();
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
		try {
			tree.getParent().endExecution();
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}
	
	private void execute(){
		System.out.println("Executing action");					
	}
}
