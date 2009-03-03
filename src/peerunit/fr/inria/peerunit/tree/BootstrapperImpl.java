package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import fr.inria.peerunit.tree.btree.BTree;
import fr.inria.peerunit.util.TesterUtil;

/** BooStrapper allow to regiter the tester. It provide a Id for each tester
 * @author Eduardo
 *
 */
public class BootstrapperImpl   implements  Bootstrapper,Serializable  {
	private static final long serialVersionUID = 1L;

	private AtomicInteger registered = new AtomicInteger(0);
	
	private static int expectedTesters=TesterUtil.getExpectedPeers();
	
	private Map<Integer,TreeTester> testers = new HashMap<Integer,TreeTester>();
	
	private Long time;
	
	/**
	 * Caching global variables
	 */
	private Map<Integer, Object> cacheMap = new ConcurrentHashMap<Integer, Object>();
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
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

	/**
	 * Export the BootStrapper remote object. Accepts calls on specific port 1099. 
	 * @param boot BootStrapperImpl
	 */
	private void startNet(BootstrapperImpl boot) {
		Bootstrapper stub=null;
		try {
			stub = (Bootstrapper) UnicastRemoteObject.exportObject(
					boot, 0);
			Registry registry = LocateRegistry.createRegistry(1099);

			registry.bind("Bootstrapper", stub);
			//Naming.rebind("//"+TesterUtil.getServerAddr()+"/Bootstrapper", boot);
		} catch (RemoteException e) {
			e.printStackTrace();		
		}  catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see fr.inria.peerunit.tree.Bootstrapper#register(fr.inria.peerunit.tree.TreeTester)
	 */
	public synchronized int register(TreeTester t)	throws RemoteException {		
		int id = registered.getAndIncrement();		
		
		testers.put(id, t);
		System.out.println("New Registered ID: " + id+" for "+t);
		return id;
	}
	
	/**
	 * Return the register
	 * @return
	 */
	public int getRegistered(){
		return registered.get();
	}
	
	/**
	 * Build the tester tree
	 */
	private void buildTree(){
		this.time=System.currentTimeMillis();			
		BTree btree=new BTree(TesterUtil.getTreeOrder());
		TreeTester t,parent,root;
		boolean isRoot;
		for (Integer i =0;i<Integer.valueOf(expectedTesters);i++) {		
			btree.insert(i);			
		}		
		this.time=System.currentTimeMillis()-this.time;		
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
		System.out.println("Construction time "+this.time+" msec");
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.tree.Bootstrapper#put(java.lang.Integer, java.lang.Object)
	 */
	public void put(Integer key, Object object) throws RemoteException {		
		cacheMap.put(key, object);
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.tree.Bootstrapper#get(java.lang.Integer)
	 */
	public Object get(Integer key) throws RemoteException {
		return cacheMap.get(key);
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.tree.Bootstrapper#getCollection()
	 */
	public Map<Integer, Object> getCollection() throws RemoteException {
		return cacheMap;
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.tree.Bootstrapper#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) throws RemoteException {
		return cacheMap.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.tree.Bootstrapper#clearCollection()
	 */
	public void clearCollection() throws RemoteException {
		cacheMap.clear();
	}	
}
