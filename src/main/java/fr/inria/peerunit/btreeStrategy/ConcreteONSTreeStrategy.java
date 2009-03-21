/**
 * 
 */
package fr.inria.peerunit.btreeStrategy;

import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import fr.inria.peerunit.onstree.stationTree.Station;
import fr.inria.peerunit.onstree.stationTree.StationRoot;
import fr.inria.peerunit.onstree.stationTree.StationTree;
import fr.inria.peerunit.onstree.stationTree.StationTreeBuilder;
import fr.inria.peerunit.onstree.testerTree.TesterNodeHead_be;
import fr.inria.peerunit.onstree.testerTree.TesterTreeBuilder;
import fr.inria.peerunit.util.TesterUtil;

/** This strategy allow to use the Optimized Network Station Tree
 * @author jeremy
 *
 */
public class ConcreteONSTreeStrategy implements TreeStrategy
{
	private TesterNodeHead_be testerNH;

	/**
	 * Constructor by default
	 */
	public ConcreteONSTreeStrategy()
	{
		
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.btreeStrategy.TreeStrategy#buildTree()
	 */
	public void buildTree()
	{
		System.out.println("****************************************");
		System.out.println("* Create all Station");

		
		Station st5 = new Station("", "192.168.1.34", "5");
		Station st1 = new Station("192.168.1.1/10.4.0.1/88.77.30.1",
				"88.77.30.15", "1");
		Station st2 = new Station("192.168.1.1/10.4.0.1/88.77.30.1/84.109.122.8/88.112.56.7/10.6.3.1",
				"10.6.3.16", "2");
		Station st8 = new Station("192.168.1.1/10.4.0.1/88.77.30.1/84.109.122.8/88.112.56.7/10.6.3.1/78.09.87.7",
				"78.09.87.20", "8");
		Station st9 = new Station("192.168.1.1/10.4.0.1/88.77.30.1/84.109.122.8/88.112.56.7/10.6.3.1/78.09.87.7",
				"78.09.87.100", "9");		
		Station st10 = new Station("192.168.1.1/10.4.0.1/88.77.30.1/84.109.122.8/88.112.56.7/10.6.3.1/78.09.87.7",
				"78.09.87.200", "10");		
		Station st3 = new Station("192.168.1.1/10.4.0.1/88.77.30.1/84.109.122.8/88.112.56.7/10.6.3.1",
				"10.6.3.17", "3");
		Station st4 = new Station("192.168.1.1/10.0.1.1/87.78.12.10",
				"87.78.12.21", "4");
		Station st6 = new Station("192.168.3.1/10.0.3.1/81.71.12.10/10.4.1.1",
				"81.28.12.62", "6");
		Station st7 = new Station("192.168.3.1", "85.38.12.61", "7");

		List<Station> listStation = new ArrayList<Station>();
		listStation.add(st5);
		listStation.add(st1);
		listStation.add(st2);
		listStation.add(st8);		
		listStation.add(st9);				
		listStation.add(st3);
		listStation.add(st4);
		listStation.add(st6);
		listStation.add(st7);
		listStation.add(st6);
		listStation.add(st10);		

		// Rooter tree
		StationRoot stRoot = new StationRoot("192.168.1.14", "BoutStrap");
		StationTreeBuilder builder = new StationTreeBuilder();
		builder.buildNetTree(listStation, stRoot);

		// Station tree
		StationTree stTree = builder.buildStationTree(stRoot);

		if(TesterUtil.getStationTreeTrace() == 1)
		{
			stTree.print();
		}
		
		// tester tree
		TesterTreeBuilder testTreeBuilder = new TesterTreeBuilder();
		// parameter tester number = 165 for 11 stations (15 tester max by station)
		testerNH = testTreeBuilder.buildTesterTree(stTree, TesterUtil.getExpectedPeers());
		
		if(TesterUtil.getStationTreeTrace() == 1)
		{
			try
			{			
				PrintWriter out=new PrintWriter(new FileWriter("routersTree.txt"));
				out.println("digraph myTree {");
				out.println(stRoot.graphicPrintRouters());
				out.println("}");		
				out.close();
				
				out=new PrintWriter(new FileWriter("stationTree.txt"));
				out.println("digraph myTree {");
				out.println(stTree.graphicPrint());
				out.println("}");			
				out.close();		
				
				out=new PrintWriter(new FileWriter("TesterTree.txt"));
				out.println("digraph myTree {");
				out.println(testerNH.graphicPrintTester());
				out.println("}");			
				out.close();	
				
	            // génération des images
				Runtime.getRuntime().exec("dot -Tpng routersTree.txt -o RoutersGraph.png");		
				Runtime.getRuntime().exec("eog stationsGraph.png");	
				Runtime.getRuntime().exec("dot -Tpng TesterTree.txt -o TesterTree.png");				//System.out.println(stTree.graphicPrint());
	
			} 
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.btreeStrategy.TreeStrategy#getNode(java.lang.Integer)
	 */
	public AbstractBTreeNode getNode(Integer i)
	{
		return testerNH.getNode(i);
	}

	/* (non-Javadoc)
	 * @see fr.inria.peerunit.btreeStrategy.TreeStrategy#getNodesSize()
	 */
	public int getNodesSize()
	{
		return testerNH.getNodesSize();
	}

}
