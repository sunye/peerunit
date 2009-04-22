package fr.inria.peerunit.btreeStrategy;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import fr.inria.peerunit.btree.BTree;
import fr.inria.peerunit.btree.Node;
import fr.inria.peerunit.btree.TreeElements;
import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author Veronique Pelleau
 * @author Aboubakar Koïta
 */

public class ConcreteBtreeStrategy implements TreeStrategy {

	
	private BTree btree;
	
	private AtomicInteger registered = new AtomicInteger(0);
	
	private static int expectedTesters=TesterUtil.instance.getExpectedTesters();
	
	private Map<Integer,Node> nodes = new HashMap<Integer,Node>();	
	
	private static final Logger log = Logger.getLogger(CoordinatorImpl.class
			.getName());
	private static Long time;	
	
	
	public ConcreteBtreeStrategy() {
		btree = new BTree(TesterUtil.instance.getTreeOrder());
	}
	
	public void buildTree() {
		btree.buildTree();
	}

	public AbstractBTreeNode getNode(Integer i) {
		return btree.getNode(i);
	}

	public int getNodesSize() {
		return btree.nodes.size();
	}

	public AbstractBTreeNode getNode(Object key) {  // XXX
		return getNode((Integer)key);
	}
	
	public void setCommunication(){	
		Node node;			
		
		for(Integer key:nodes.keySet()){
			TreeElements te=new TreeElements();	
			if(!getNode(key).isLeaf()){
				for(AbstractBTreeNode child:getNode(key).getChildren()){
					if(child!=null){
						te.setChildren(nodes.get(child.getId()));
					}
				}
			}else
			{
				te.setChildren(null);
			}
			
			if(!getNode(key).isRoot()){
				int parentId=getNode(key).getParent().getId();
				te.setParent(nodes.get(parentId));
			}
			/**
			 * Now we inform Node its tree elements. 
			 */			
			node=nodes.get(key);
			System.out.println("[Bootstrapper] Contacting Node "+node);
			try {
				node.setElements(getNode(key),te);
			} catch (RemoteException e) {				
				e.printStackTrace();
			}
		}
	}
	public int register(Node node) throws RemoteException {
		int id = registered.getAndIncrement();		
		if(id<getNodesSize()){
			nodes.put(id, node);
			System.out.println("[Bootstrapper] New Registered ID: " + id+" for "+node);
			return id;
		}else{
			System.out.println("[Bootstrapper] Not registerd " + id+" for "+node);
			return Integer.MAX_VALUE;
		}		
	}

	public int getRegistered() {
		return registered.intValue();
	}
}
