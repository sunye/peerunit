package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import fr.inria.peerunit.tree.btree.BTree;
import fr.inria.peerunit.util.TesterUtil;

public class BootstrapperImpl  implements  Bootstrapper,Serializable {
	private static final long serialVersionUID = 1L;

	private AtomicInteger registered = new AtomicInteger(0);
	
	private static int expectedTesters=TesterUtil.getExpectedPeers();
	
	private Map<Integer,TreeTester> testers = new HashMap<Integer,TreeTester>();
	
	private Long time;
	
	protected BootstrapperImpl() throws RemoteException {
		super();		
	}
	
	public static void main(String[] args) throws RemoteException {	
		BootstrapperImpl boot=new BootstrapperImpl();
		boot.startNet(boot);	
		
		while (boot.getRegistered() < expectedTesters) {
			try {								
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[Bootstrapper] Lets see the tree !");
		boot.buildTree();
		System.out.println("[Bootstrapper] Finished !");
	}	

	private void startNet(BootstrapperImpl boot) {
		Bootstrapper stub=null;
		try {
			stub = (Bootstrapper) UnicastRemoteObject.exportObject(
					boot, 0);
			Registry registry = LocateRegistry.createRegistry(1099);

			registry.bind("Bootstrapper", stub);			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized int register(TreeTester t)	throws RemoteException {		
		int id = registered.getAndIncrement();		
		
		testers.put(id, t);
		System.out.println("New Registered ID: " + id+" for "+t);
		return id;
	}
	
	public int getRegistered(){
		return registered.get();
	}
	
	private void buildTree(){
		this.time=System.currentTimeMillis();			
		BTree btree=new BTree(TesterUtil.getTreeOrder());
		TreeTester t,parent,root;
		boolean isRoot;
		for (Integer i =0;i<Integer.valueOf(expectedTesters);i++) {		
			btree.insert(i);			
		}		
		for (Integer i =0;i<Integer.valueOf(expectedTesters);i++) {
			System.out.println(btree.find(i));			
			btree.getTreeElements(i);			
			
			t=testers.get(i);
			parent=testers.get(btree.getTreeElements(i).getParent());
			root=testers.get(btree.getTreeElements(i).getRoot());
			
			if(i.intValue()==btree.getTreeElements(i).getRoot().intValue()){
				System.out.println(i+" Me root "+t);
				isRoot=true;
			}	else{
				isRoot=false;
				/**
				 * Fix neighbor parent.
				 * Sometimes neighbors don't have parent, however, root is the one.
				 * */
				System.out.println(i+" Me "+t);
				System.out.println("Parent "+parent);
				if(parent==null){
					System.out.println("Parent fixed.");
					parent=root;
				}
			}
			
			try {
				t.setTreeElements(new TreeElements(parent,root),isRoot);
			} catch (RemoteException e) {				
				e.printStackTrace();
			}
		}
		System.out.println("Construction time "+(System.currentTimeMillis()-this.time));
	}
}
