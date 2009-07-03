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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//import mtr.MTRLib;
import fr.inria.peerunit.btree.Node;
import fr.inria.peerunit.btree.TreeElements;
import fr.inria.peerunit.onstree.stationTree.Station;
import fr.inria.peerunit.onstree.stationTree.StationContainer;
import fr.inria.peerunit.onstree.stationTree.StationRoot;
import fr.inria.peerunit.onstree.stationTree.StationTree;
import fr.inria.peerunit.onstree.stationTree.StationTreeBuilder;
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
		InputStream input = ConcreteONSTreeStrategy.class
				.getResourceAsStream("/hosts.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String host = null;
		List<Station> listStation = new ArrayList<Station>();
		int cpt = 1;
		try {
			while ((host = reader.readLine()) != null) {
				String route=traceroute.TraceRoute.getRoute("-n", host);
				Station station = new Station(route,host, Integer.toString(cpt++));
				listStation.add(station);
				
				System.out.println("Route="+route);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// Rooter tree
		StationRoot stRoot = new StationRoot(TesterUtil.instance.getOnStationRoot(), "Bootstrap");
		StationTreeBuilder builder = new StationTreeBuilder();
		builder.buildNetTree(listStation, stRoot);

		// Station tree
		StationTree stTree = builder.buildStationTree(stRoot);

		if (TesterUtil.instance.getStationTreeTrace() == 1) {
			stTree.print();
		}
		// tester tree
		TesterTreeBuilder testTreeBuilder = new TesterTreeBuilder();
		// parameter tester number = 165 for 11 stations (15 tester max by
		// station)
		testerNH = testTreeBuilder.buildTesterTree(stTree, TesterUtil.instance
				.getExpectedTesters());
		buildIPNodeHeadMap(testerNH);
		printStTree(stTree);
		if (TesterUtil.instance.getStationTreeTrace() == 1) {
			try {
				PrintWriter out = new PrintWriter(new FileWriter(
						"routersTree.txt"));
				out.println("digraph myTree {");
				out.println(stRoot.graphicPrintRouters());
				out.println("}");
				out.close();

				out = new PrintWriter(new FileWriter("stationTree.txt"));
				out.println("digraph myTree {");
				out.println(stTree.graphicPrint());
				out.println("}");
				out.close();

				out = new PrintWriter(new FileWriter("TesterTree.txt"));
				out.println("digraph myTree {");
				out.println(testerNH.graphicPrintTester());
				out.println("}");
				out.close();
				
				// génération des images
				Runtime.getRuntime().exec(
						"dot -Tpng routersTree.txt -o RoutersGraph.png");
				Runtime.getRuntime().exec(
						"dot -Tpng stationTree.txt -o stationTree.png");
				Runtime.getRuntime().exec(
						"dot -Tpng TesterTree.txt -o TesterTree.png");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void buildIPNodeHeadMap(TesterNodeHead tester) {
		ipNodeHeadMap.put(tester.getIP(), tester);
		System.out.println("NodeHeapIP="+tester.getIP());
		System.out.println("childs");		
		for (TesterNodeHead nodeHeadBe : tester.getListTesterNodeHead()) {
			System.out.println(nodeHeadBe.getIP());			
		}
		System.out.println("\n\n");
		for (TesterNodeHead nodeHeadBe : tester.getListTesterNodeHead()) {
			buildIPNodeHeadMap(nodeHeadBe);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.inria.peerunit.btreeStrategy.TreeStrategy#getNode(java.lang.Integer)
	 */
	public AbstractBTreeNode getNode(Integer i) {
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
			System.out.println("Current Key="+key);
			TreeElements te = new TreeElements();
			AbstractBTreeNode node = getNode(key);
			if (!node.isLeaf()) {
				TesterNodeHead nodeHead=(TesterNodeHead)getNode(key);
				List<TesterNodeHead> childsNheads =nodeHead.getListTesterNodeHead();
				System.out.println("Setcommunication NodeHead="+nodeHead.getIP());				
				for (TesterNodeHead nodeHeadBe : childsNheads) {
					System.out.println("Setcommunication Childs="+nodeHeadBe.getIP());					
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
			try {
				remoteNode.setElements(getNode(key), te);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

    private void printStTree(fr.inria.peerunit.onstree.stationTree.StationContainer station)
    {
		System.out.println("Station IP="+station.getStation().getIp());
    	for(fr.inria.peerunit.onstree.stationTree.Node node:station.getListChildStation())
    	{
    		System.out.println("Station child IP="+node.getStParent().getIp());    		
    	}

    	
    	System.out.println("\n\n");
    	
    	for(fr.inria.peerunit.onstree.stationTree.Node node:station.getListChildStation())
    	{
    		printStTree(node);
    	}	
    }
}
