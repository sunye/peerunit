package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;



public class TreeTesterImpl  implements TreeTester,Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	
	private TreeElements tree=null;

	public static void main(String args[]) throws Exception{
		TreeTesterImpl tt= new TreeTesterImpl();
		tt.startNet();		
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
	public synchronized void setTreeElements(TreeElements tree) throws RemoteException{
		this.tree=tree;		
	}
	
	public void startExecution() throws RemoteException{
		
	}
	
	public int getId(){
		return id;
	}
	
	
}
