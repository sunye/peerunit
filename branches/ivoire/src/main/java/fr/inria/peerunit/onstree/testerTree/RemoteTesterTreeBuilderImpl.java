package fr.inria.peerunit.onstree.testerTree;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.peerunit.btree.Bootstrapper;
import fr.inria.peerunit.btreeStrategy.ConcreteONSTreeStrategy;
import fr.inria.peerunit.onstree.stationTree.Station;
import fr.inria.peerunit.onstree.stationTree.StationRoot;
import fr.inria.peerunit.onstree.stationTree.StationTree;
import fr.inria.peerunit.onstree.stationTree.StationTreeBuilder;
import fr.inria.peerunit.util.TesterUtil;

public class RemoteTesterTreeBuilderImpl implements RemoteTesterTreeBuilder,
		Serializable {

	public RemoteTesterTreeBuilderImpl() {
		try {
			
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}
			Registry registry = LocateRegistry.createRegistry(1099);			
			RemoteTesterTreeBuilder stub = (RemoteTesterTreeBuilder) UnicastRemoteObject
					.exportObject(this, 0);
			registry.rebind("TesterTreeBuilder", stub);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TesterNodeHead getTesterTreeRoot() throws RemoteException {

		InputStream input = ConcreteONSTreeStrategy.class
				.getResourceAsStream("/hosts.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String host = null;
		List<Station> listStation = new ArrayList<Station>();
		int cpt = 1;
		try {
			while ((host = reader.readLine()) != null) {
				String route = traceroute.TraceRoute.getRoute("-n", host);
				Station station = new Station(route, host, Integer
						.toString(cpt++));
				listStation.add(station);

				System.out.println("Route=" + route);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// Rooter tree
		StationRoot stRoot = new StationRoot(TesterUtil.instance
				.getOnStationRoot(), "RootStation");
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
		TesterNodeHead testerNH = testTreeBuilder.buildTesterTree(stTree,
				TesterUtil.instance.getExpectedTesters());
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
		return testerNH;
	}

	public HashMap<String, TesterNodeHead> getIPNodeHeadMap(
			TesterNodeHead testerNodeHead) throws RemoteException {
		HashMap<String, TesterNodeHead> iNodeHeadMap = new HashMap<String, TesterNodeHead>();
		buildIPNodeheapMap(testerNodeHead, iNodeHeadMap);
		return iNodeHeadMap;
	}

	private void buildIPNodeheapMap(TesterNodeHead testerNodeHead,
			HashMap<String, TesterNodeHead> iNodeHeadMap) {
		iNodeHeadMap.put(testerNodeHead.getIP(), testerNodeHead);
		System.out.println("NodeHeapIP=" + testerNodeHead.getIP());
		System.out.println("childs");
		for (TesterNodeHead nodeHeadBe : testerNodeHead.getListTesterNodeHead()) {
			System.out.println(nodeHeadBe.getIP());
		}
		System.out.println("\n\n");
		for (TesterNodeHead nodeHeadBe : testerNodeHead.getListTesterNodeHead()) {
			buildIPNodeheapMap(nodeHeadBe, iNodeHeadMap);
		}
	}

	private void printStTree(
			fr.inria.peerunit.onstree.stationTree.StationContainer stationContainer) {
		System.out.println("Station IP=" + stationContainer.getStation().getIp());
		for (fr.inria.peerunit.onstree.stationTree.Node node : stationContainer
				.getListChildStation()) {
			System.out
					.println("Station child IP=" + node.getStParent().getIp());
		}

		System.out.println("\n\n");

		for (fr.inria.peerunit.onstree.stationTree.Node node : stationContainer
				.getListChildStation()) {
			printStTree(node);
		}
	}

	public static void main(String[] args) throws RemoteException,
			NotBoundException {
		if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}		
		System.out.println("Starting RemoteTesterBuilder");
		RemoteTesterTreeBuilder remoteTesterTreeBuilder = new RemoteTesterTreeBuilderImpl();
		System.out.println("RemoteTesterBuilder started at address:"
				+ System.getProperty("java.rmi.server.hostname"));
		Registry registry = LocateRegistry.getRegistry(TesterUtil.instance
				.getOnStationRoot());
		RemoteTesterTreeBuilder remoteTesterTreeBuilder1 = (RemoteTesterTreeBuilder) registry
				.lookup("TesterTreeBuilder");
		TesterNodeHead testerNH = remoteTesterTreeBuilder1.getTesterTreeRoot();
		HashMap<String, TesterNodeHead> ipNodeHeadMap = remoteTesterTreeBuilder1
				.getIPNodeHeadMap(testerNH);
	}
}
