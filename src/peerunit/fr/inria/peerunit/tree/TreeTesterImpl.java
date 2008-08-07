package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;



public class TreeTesterImpl  implements TreeTester,Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public int id;
	
	private TreeElements tree=new TreeElements();
	
	private boolean amIRoot=false;

	public static void main(String args[]) throws Exception{
		TreeTesterImpl tt= new TreeTesterImpl();
		tt.startNet();		
		tt.talkToParent();
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
		
	}
	
	public int getId()throws RemoteException {
		return id;
	}

	public void setChildren(TreeTester tester) throws RemoteException {
		tree.add(tester,id);		
	}	
	
	private void talkToParent(){
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
}
