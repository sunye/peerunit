package fr.inria.peerunit.btree;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;
import fr.inria.peerunit.btreeStrategy.ConcreteBtreeStrategy;
import fr.inria.peerunit.btreeStrategy.Context;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.ArchitectureImpl;
import fr.inria.peerunit.btreeStrategy.ConcreteONSTreeStrategy;
import fr.inria.peerunit.util.TesterUtil;

/**
 * 
 * @author Eduardo Almeida, Veronique PELLEAU
 * @version 1.0
 * @since 1.0
 */
public class BootstrapperImpl extends ArchitectureImpl implements  Bootstrapper, Serializable  {

	private static final long serialVersionUID = 1L;

	private AtomicInteger registered = new AtomicInteger(0);
	
	private static int expectedTesters=TesterUtil.getExpectedPeers();
	
	private Map<Integer,Node> nodes = new HashMap<Integer,Node>();	
	
	private static final Logger log = Logger.getLogger(CoordinatorImpl.class
			.getName());
	
	//static BTree btree=new BTree(TesterUtil.getTreeOrder());
	static Context context;
	
	private static Long time;	
	
	protected BootstrapperImpl() throws RemoteException {
		super();		
	}
	
	public static void main(String[] args) throws RemoteException {	
		// Log creation
		FileHandler handler;
		try {
			handler = new FileHandler(TesterUtil.getLogfile());
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);
			log.setLevel(Level.parse(TesterUtil.getLogLevel()));

		} catch (SecurityException e) {
			log.log(Level.SEVERE, "SecurityException", e);
			e.printStackTrace();
		} catch (IOException e) {
			log.log(Level.SEVERE, "IOException", e);
			e.printStackTrace();
		}

		// Check tester.coordination property
		ckeckFileProperty();
		
		BootstrapperImpl boot=new BootstrapperImpl();
		boot.startNet(boot);	
		System.out.println("[Bootstrapper] Lets see the tree !");		

		time=System.currentTimeMillis();
		
		switch (TesterUtil.getTreeStrategy()) {
		case 1:
			context = new Context(new ConcreteBtreeStrategy());
			System.out.println("[Bootstrapper] Strategy BTree !");
			break;

		case 2:
			context = new Context(new ConcreteONSTreeStrategy());
			System.out.println("[Bootstrapper] Strategy optimized station tree !");
			break;
			
		case 3:
			context = new Context(new ConcreteONSTreeStrategy());
			break;
			
		default:
			context = new Context(new ConcreteBtreeStrategy());
			break;
		}
		
		context.buildTree();
		
		time=System.currentTimeMillis()-time;		
		System.out.println("[Bootstrapper] Built tree in: "+time+" msec");		
		System.out.println("[Bootstrapper] Nodes expected :"+context.getNodesSize());
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
		
		if(id<context.getNodesSize()){
			nodes.put(id, node);
			System.out.println("[Bootstrapper] New Registered ID: " + id+" for "+node);
			return id;
		}else{
			System.out.println("[Bootstrapper] Not registerd " + id+" for "+node);
			return Integer.MAX_VALUE;
		}		
	}
	
	/**
	 * Returns the current number of registered nodes
	 * @return the current number of registered nodes
	 */
	public int getRegistered(){
		return registered.get();
	}
	
	private void setCommunication(){	
		Node node;			
		
		for(Integer key:nodes.keySet()){
			TreeElements te=new TreeElements();	
			if(!context.getNode(key).isLeaf()){
				for(AbstractBTreeNode child:context.getNode(key).getChildren()){
					if(child!=null){
						te.setChildren(nodes.get(child.getId()));
					}
				}
			}else
			{
				te.setChildren(null);
			}
			
			if(!context.getNode(key).isRoot()){
				int parentId=context.getNode(key).getParent().getId();
				te.setParent(nodes.get(parentId));
			}
			/**
			 * Now we inform Node its tree elements. 
			 */			
			node=nodes.get(key);
			System.out.println("[Bootstrapper] Contacting Node "+node);
			try {
				node.setElements(context.getNode(key),te);
			} catch (RemoteException e) {				
				e.printStackTrace();
			}
		}
	}

	public void put(Integer key, Object object) throws RemoteException {	
		System.out.println("[Bootstrapper] Caching object "+object);
		cacheMap.put(key, object);
	}

	/**
	 * Check that the test.coordination property is correctly record in the configuration file tester.properties
	 * i.e is equals at 1 for the distributed coordination
	 */
	private static void ckeckFileProperty() {
		if (TesterUtil.getCoordinationType() != 1) {
			log.log(Level.WARNING, "Running the distributed coordination but using the centralized coordination in the configuration file tester.properties. \n" +
					"Set property test.coordination=1 to use distributed coordination.");
		}
	}
	
}
