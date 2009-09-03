/**
 * 
 */
package fr.inria.peerunit.btreeStrategy;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//import mtr.MTRLib;
import fr.inria.peerunit.btree.Bootstrapper;
import fr.inria.peerunit.btree.Node;
import fr.inria.peerunit.btree.TreeElements;
import fr.inria.peerunit.onstree.stationTree.Station;
import fr.inria.peerunit.onstree.stationTree.StationContainer;
import fr.inria.peerunit.onstree.stationTree.StationRoot;
import fr.inria.peerunit.onstree.stationTree.StationTree;
import fr.inria.peerunit.onstree.stationTree.StationTreeBuilder;
import fr.inria.peerunit.onstree.testerTree.RemoteTesterTreeBuilder;
import fr.inria.peerunit.onstree.testerTree.TesterNodeHead;
import fr.inria.peerunit.onstree.testerTree.TesterTreeBuilder;
import fr.inria.peerunit.util.TesterUtil;

/**
 * This strategy allow to use the Optimized Network Station Tree
 * 
 * @author jeremy
 * @author Aboubakar Koïta
 * 
 */
public class ConcreteONSTreeStrategy implements TreeStrategy {
	private TesterNodeHead testerNH;
	private HashMap<String, TesterNodeHead> ipNodeHeadMap = new HashMap<String, TesterNodeHead>();
	private HashMap<String, Node> remotesNodesMap = new HashMap<String, Node>();
	private AtomicInteger registered = new AtomicInteger(0);

	// private static int expectedTesters =
	// TesterUtil.instance.getExpectedTesters();

	/**
	 * Constructor by default
	 */
	public ConcreteONSTreeStrategy() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.inria.peerunit.btreeStrategy.TreeStrategy#buildTree()
	 */
	public void buildTree() {
		try {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}						
			Registry registry = LocateRegistry.getRegistry(TesterUtil.instance
					.getOnStationRoot());
			RemoteTesterTreeBuilder remoteTesterTreeBuilder = (RemoteTesterTreeBuilder) registry
					.lookup("TesterTreeBuilder");
			testerNH = remoteTesterTreeBuilder.getTesterTreeRoot();
			ipNodeHeadMap = remoteTesterTreeBuilder.getIPNodeHeadMap(testerNH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.inria.peerunit.btreeStrategy.TreeStrategy#getNode(java.lang.Integer)
	 */
	public AbstractBTreeNode getNode(Integer i) {
		System.out.println("GetNodeInteger");		
		return testerNH.getNode(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.inria.peerunit.btreeStrategy.TreeStrategy#getNodesSize()
	 */
	public int getNodesSize() {
		return testerNH.getNodesSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.inria.peerunit.btreeStrategy.TreeStrategy#getNode(java.lang.Object)
	 */
	public AbstractBTreeNode getNode(Object key) {
		if (key instanceof Integer) {
			return getNode((Integer) key);
		} else {
			return getNode((String) key);
		}

	}

	public AbstractBTreeNode getNode(String ip) {
		System.out.println("Key="+ip + "  "+ipNodeHeadMap.get(ip).getIP());
		return ipNodeHeadMap.get(ip);
	}

	public int register(Node node) throws RemoteException {
		remotesNodesMap.put(node.getIP(), node);
		TesterNodeHead nodeHeadBe = ipNodeHeadMap.get(node.getIP());
		registered.incrementAndGet();
		return nodeHeadBe.getId();
	}

	public int getRegistered() {
		return registered.intValue();
	}

	public void setCommunication() {
		for (String key : ipNodeHeadMap.keySet()) {
			System.out.println("Current Key=" + key);			
			TreeElements te = new TreeElements();
			AbstractBTreeNode node = getNode(key);
			if (!node.isLeaf()) {
				TesterNodeHead nodeHead = (TesterNodeHead) getNode(key);
				List<TesterNodeHead> childsNheads = nodeHead
						.getListTesterNodeHead();
				System.out.println("Setcommunication NodeHead="
						+ nodeHead.getIP());
				for (TesterNodeHead nodeHeadBe : childsNheads) {
					System.out.println("Setcommunication Childs="
							+ nodeHeadBe.getIP());
					if (nodeHeadBe != null) {
						Node node2 = remotesNodesMap.get(nodeHeadBe.getIP());
						te.setChildren(node2);
					}
				}
				System.out.println("\n\n");
			} else {
				te.setChildren(null);
			}
			if (!node.isRoot()) {
				TesterNodeHead parent = (TesterNodeHead) node.getParent();
				te.setParent(remotesNodesMap.get(parent.getIP()));
			}

			System.out.println("[Bootstrapper] Befor Contacting Node ");			
			/**
			 * Now we inform Node its tree elements.
			 */
			Node remoteNode = remotesNodesMap.get(key);			
			System.out.println("[Bootstrapper] Contacting Node " + remoteNode);
			System.out.println(getNode(key));			
			try {
				remoteNode.setElements(getNode(key), te);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}
