package fr.inria.peerunit.btree;

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

import fr.inria.peerunit.util.TesterUtil;

public class BootstrapperImpl   implements  Bootstrapper,Serializable  {
	private static final long serialVersionUID = 1L;

	private AtomicInteger registered = new AtomicInteger(0);
	
	private static int expectedTesters=TesterUtil.getExpectedPeers();
	
	private Map<Integer,Node> nodes = new HashMap<Integer,Node>();
	
	private Long time;
	
	/**
	 * Caching global variables
	 */
	private Map<Integer, Object> cacheMap = new ConcurrentHashMap<Integer, Object>();
	
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
		}  catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized int register(Node node)	throws RemoteException {		
		int id = registered.getAndIncrement();				
		nodes.put(id, node);
		System.out.println("New Registered ID: " + id+" for "+node);
		return id;
	}
	
	public int getRegistered(){
		return registered.get();
	}
	
	private void buildTree(){
		this.time=System.currentTimeMillis();			
		BTree btree=new BTree(TesterUtil.getTreeOrder());
		btree.buildTree();
		Node node;

		this.time=System.currentTimeMillis()-this.time;		
		for (Integer i =0;i<Integer.valueOf(expectedTesters);i++) {	
			
			TreeElements te=new TreeElements();
			for(BTreeNode child:btree.getNode(i).children){
				te.setChildren(nodes.get(child.id));	
			}
			
			/**
			 * Now we inform Node its tree elements. 
			 */
			node=nodes.get(i);
			try {
				node.setElements(btree.getNode(i),te);
			} catch (RemoteException e) {				
				e.printStackTrace();
			}
		}	
		System.out.println("Construction time "+this.time+" msec");
	}

	public void put(Integer key, Object object) throws RemoteException {		
		cacheMap.put(key, object);
	}

	public Object get(Integer key) throws RemoteException {
		return cacheMap.get(key);
	}

	public Map<Integer, Object> getCollection() throws RemoteException {
		return cacheMap;
	}

	public boolean containsKey(Object key) throws RemoteException {
		return cacheMap.containsKey(key);
	}

	public void clearCollection() throws RemoteException {
		cacheMap.clear();
	}	
}
