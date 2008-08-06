package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import fr.inria.peerunit.tree.oldbtree.TreeElements;


public class TreeTesterImpl  implements TreeTester,Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	
	private TreeElements tree;

	public static void main(String args[]) throws Exception{
		TreeTesterImpl tt= new TreeTesterImpl();
		tt.startNet();		
	}
	
	private  void startNet(){		

		try {
						
			Registry registry = LocateRegistry.getRegistry("172.16.9.101");
			Bootstrapper boot = (Bootstrapper) registry.lookup("Bootstrapper");	
			id=boot.register(this);
			System.out.println("My ID is: "+id);
			UnicastRemoteObject.exportObject(this);			
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} 
	}	
	
	/**
	 * Elements of the BTree
	 */	
	public void setTreeElements(TreeElements tree) throws RemoteException{
		this.tree=tree;
	}
	
	public void startExecution() throws RemoteException{
		
	}
	
	public int getId(){
		return id;
	}
}
