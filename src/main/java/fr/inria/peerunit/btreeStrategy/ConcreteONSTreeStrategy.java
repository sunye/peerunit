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

import mtr.MTRLib;
import fr.inria.peerunit.btree.Node;
import fr.inria.peerunit.btree.TreeElements;
import fr.inria.peerunit.onstree.stationTree.Station;
import fr.inria.peerunit.onstree.stationTree.StationRoot;
import fr.inria.peerunit.onstree.stationTree.StationTree;
import fr.inria.peerunit.onstree.stationTree.StationTreeBuilder;
import fr.inria.peerunit.onstree.testerTree.TesterNodeHead_be;
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
	private TesterNodeHead_be testerNH;
	private HashMap<String, TesterNodeHead_be> ipNodeHeadMap = new HashMap<String, TesterNodeHead_be>();
	private HashMap<String, Node> remotesNodesMap = new HashMap<String, Node>();
	private AtomicInteger registered = new AtomicInteger(0);
	//private static int expectedTesters = TesterUtil.instance.getExpectedTesters();

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
		String hostsFile = TesterUtil.instance.getHostsFilePath();
		InputStream input = ConcreteONSTreeStrategy.class
				.getResourceAsStream(hostsFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String host = null;
		List<Station> listStation = new ArrayList<Station>();
		int cpt = 1;
		try {
			while ((host = reader.readLine()) != null) {
				Station station = new Station(MTRLib.getRoute(host), host,
						Integer.toString(cpt++));
				listStation.add(station);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// Rooter tree
		StationRoot stRoot = new StationRoot("192.168.1.14", "BoutStrap");
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

	private void buildIPNodeHeadMap(TesterNodeHead_be tester) {
		ipNodeHeadMap.put(tester.getIP(), tester);
		for (TesterNodeHead_be nodeHeadBe : tester.getListTesterNodeHead()) {
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
		TesterNodeHead_be nodeHeadBe = ipNodeHeadMap.get(node.getIP());
		registered.incrementAndGet();
		return nodeHeadBe.getId();
	}

	public int getRegistered() {
		return registered.intValue();
	}

	public void setCommunication() {
		for (String key : ipNodeHeadMap.keySet()) {
			TreeElements te = new TreeElements();
			AbstractBTreeNode node = getNode(key);
			if (!node.isLeaf()) {
				List<TesterNodeHead_be> chilNhead = ((TesterNodeHead_be) getNode(key))
						.getListTesterNodeHead();
				for (TesterNodeHead_be nodeHeadBe : chilNhead) {
					if (nodeHeadBe != null) {
						Node node2 = remotesNodesMap.get(nodeHeadBe.getIP());
						te.setChildren(node2);
					} else {
						te.setChildren(null);
					}
				}
				if (!node.isRoot()) {
					TesterNodeHead_be parent = (TesterNodeHead_be) node
							.getParent();
					te.setParent(remotesNodesMap.get(parent.getIP()));
				}
				/**
				 * Now we inform Node its tree elements.
				 */
				Node remoteNode = remotesNodesMap.get(key);
				System.out.println("[Bootstrapper] Contacting Node "
						+ remoteNode);
				try {
					remoteNode.setElements(getNode(key), te);
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}
		}
	}
}
