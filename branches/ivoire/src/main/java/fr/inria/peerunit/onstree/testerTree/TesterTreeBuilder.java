package fr.inria.peerunit.onstree.testerTree;

import fr.inria.peerunit.onstree.stationTree.Node;
import fr.inria.peerunit.onstree.stationTree.StationContainer;
import fr.inria.peerunit.onstree.stationTree.StationRoot;
import fr.inria.peerunit.util.TesterUtil;
import fr.inria.peerunit.util.Util;

;

public class TesterTreeBuilder {
	private long ID_auto = 0;
	private int TesterNumberCreated = 0;
	private int lTesterMaxByStation =TesterUtil.instance.getMaxTesterByStation();
	private int lTesterMax = 0;

	public TesterNodeHead buildTesterTree(StationContainer sTree,
			int testNumber) {
		int iComparable = 1;
		int nodeFirstTesterNumber = 0;
		String sColor = "green";
		StationRoot stRoot = (StationRoot) sTree.getStation();
		TesterTree tree = new TesterTree();
		TesterNodeHead testerNH_root = new TesterNodeHead();
		lTesterMax = testNumber;
		if (stRoot != null && BuildPossible(sTree)) {
			nodeFirstTesterNumber = TesterNumberCreated;
			for (iComparable = nodeFirstTesterNumber; (iComparable < lTesterMaxByStation
					+ nodeFirstTesterNumber)
					&& (TesterNumberCreated < lTesterMax); iComparable++) {
				TesterNumberCreated++;
				tree.add(iComparable);
			}

			// Copy TesterNode root to TesterNodeHead
			TesterNode root = tree.getRoot();
			if (root != null) {
				// color of the dot graph
				Util.initColor();
				sColor = Util.getColor();
				testerNH_root = new TesterNodeHead(
						sTree.getStation().getIp(), root.getId(), sColor, root
								.getChildL(), root.getChildR(), root
								.getEquilibre()); // XXX	
			}
			
			if (TesterNumberCreated < lTesterMax) {				
				// create other tester tree with children station
				for (Node stationNode : sTree.getListChildStation()) {
					System.out.println("Root child="+stationNode.getStParent().getIp());					
					if (TesterNumberCreated < lTesterMax) {
						TesterNodeHead childTesterHead = createTree(stationNode);
						if (childTesterHead != null) {
							testerNH_root.addNodeHead(childTesterHead);
						}
					}
				}
			}
		}

		updateNodeParent(testerNH_root);
		return testerNH_root;
	}

	/**
	 * update parent every nodes
	 */
	private void updateNodeParent(TesterNodeHead testerNH_root) {
		testerNH_root.updateParent(null);
	}

	private TesterNodeHead createTree(Node node) {
		int iComparable = 0;
//		int BeginTesterNumber = 0;
		int BeginTesterNumber =TesterNumberCreated-1; // XXX		
		TesterTree tree = new TesterTree();
		TesterNodeHead testerNH_root = new TesterNodeHead();
		String sColor = "red";

		BeginTesterNumber = TesterNumberCreated;
		for (iComparable = BeginTesterNumber; (iComparable < lTesterMaxByStation
				+ BeginTesterNumber)
				&& (TesterNumberCreated < lTesterMax); iComparable++) {
			TesterNumberCreated++;
			tree.add(iComparable);
		}

		// Copy TesterNode root to TesterNodeHead
		TesterNode root = tree.getRoot();
		if (root != null) {
			// color of the dot graph
			sColor = Util.getColor();
			testerNH_root = new TesterNodeHead(node.getStParent().getIp(),root.getId(), sColor, root
					.getChildL(), root.getChildR(), root.getEquilibre());
			if (TesterNumberCreated < lTesterMax) {
				// create other tester tree with children station
				for (Node stationNode : node.getListChildStation()) {
					if (TesterNumberCreated < lTesterMax) {
						TesterNodeHead childTesterHead = createTree(stationNode);
						if (childTesterHead != null) {
							testerNH_root.addNodeHead(childTesterHead);
						}
					}
				}
			}
		}

		return testerNH_root;
	}

	private boolean BuildPossible(StationContainer sTree) {
		int stationNumber = 0;

		stationNumber = sTree.getChildrenNumber();
		System.out.println("Station Number : " + stationNumber);

		if ((stationNumber * 16) > lTesterMaxByStation) {
			return true;
		}
		System.out.println(" Warning : Tester number > 16 tester by station");
		return false;
	}

	public long getID_Auto() {
		return ID_auto++;
	}

	public  void setTesterNumberCreated() {
		TesterNumberCreated++;
	}

	public long getTesterNumberCreated() {
		return TesterNumberCreated;
	}
}
