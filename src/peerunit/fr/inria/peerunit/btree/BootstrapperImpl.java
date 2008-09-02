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
	
	static BTree btree=new BTree(TesterUtil.getTreeOrder());
	
	private static Long time;	
	
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
		System.out.println("[Bootstrapper] Lets see the tree !");		
		time=System.currentTimeMillis();			
		btree.buildTree();
		time=System.currentTimeMillis()-time;		
		System.out.println("[Bootstrapper] Built tree in: "+time+" msec");		
		System.out.println("[Bootstrapper] Nodes expected :"+btree.nodes.size());
		while (boot.getRegistered() < expectedTesters) {
			try {								
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		boot.setCommunication();
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
		
		if(id<btree.nodes.size()){
			nodes.put(id, node);
			System.out.println("[Bootstrapper] New Registered ID: " + id+" for "+node);
			return id;
		}else{
			System.out.println("[Bootstrapper] Not registerd " + id+" for "+node);
			return Integer.MAX_VALUE;
		}		
	}
	
	public int getRegistered(){
		return registered.get();
	}
	
	private void setCommunication(){		
		Node node;			
		for(Integer key:nodes.keySet()){
			TreeElements te=new TreeElements();			
			if(!btree.getNode(key).isLeaf()){
				for(BTreeNode child:btree.getNode(key).children){
					if(child!=null){
						System.out.println("Child id "+child.id);
						te.setChildren(nodes.get(child.id));
					}
				}
			}else
				te.setChildren(null);
			
			if(!btree.getNode(key).isRoot()){
				int parentId=btree.getNode(key).parent.id;
				te.setParent(nodes.get(parentId));
			}
			/**
			 * Now we inform Node its tree elements. 
			 */			
			node=nodes.get(key);
			System.out.println("[Bootstrapper] Contacting Node "+node);
			try {
				node.setElements(btree.getNode(key),te);
			} catch (RemoteException e) {				
				e.printStackTrace();
			}
		}
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
