package fr.inria.peerunit.onstree.testerTree;

import fr.inria.peerunit.onstree.stationTree.Node;
import fr.inria.peerunit.onstree.stationTree.StationContainer;
import fr.inria.peerunit.onstree.stationTree.StationRoot;
import fr.inria.peerunit.util.TesterUtil;
import fr.inria.peerunit.util.Util;

;

public class TesterTreeBuilder {
	private static long ID_auto = 0;
	private static int TesterNumberCreated = 0;
	private int lTesterMaxByStation =TesterUtil.instance.getMaxTesterByStation();
	private int lTesterMax = 0;

	public TesterNodeHead_be buildTesterTree(StationContainer sTree,
			int testNumber) {
		int iComparable = 1;
		int nodeFirstTesterNumber = 0;
		String sColor = "green";
		StationRoot stRoot = (StationRoot) sTree.getStation();
		TesterTree tree = new TesterTree();
		TesterNodeHead_be testerNH_root = new TesterNodeHead_be();
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
			TesterNode_be root = tree.getRoot();
			if (root != null) {
				// color of the dot graph
				Util.initColor();
				sColor = Util.getColor();
				testerNH_root = new TesterNodeHead_be(
						sTree.getStation().getIp(), root.getId(), sColor, root
								.getChildL(), root.getChildR(), root
								.getEquilibre()); // XXX	
			}

			if (TesterNumberCreated < lTesterMax) {				
				// create other tester tree with children station
				for (Node stationNode : sTree.getListChildStation()) {
					if (TesterNumberCreated < lTesterMax) {
						TesterNodeHead_be childTesterHead = createTree(stationNode);
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
	private void updateNodeParent(TesterNodeHead_be testerNH_root) {
		testerNH_root.updateParent(null);
	}

	private TesterNodeHead_be createTree(Node node) {
		int iComparable = 0;
		int BeginTesterNumber = 0;
		TesterTree tree = new TesterTree();
		TesterNodeHead_be testerNH_root = new TesterNodeHead_be();
		String sColor = "red";

		BeginTesterNumber = TesterNumberCreated;
		for (iComparable = BeginTesterNumber; (iComparable < lTesterMaxByStation
				+ BeginTesterNumber)
				&& (TesterNumberCreated < lTesterMax); iComparable++) {
			TesterNumberCreated++;
			tree.add(iComparable);
		}

		// Copy TesterNode root to TesterNodeHead
		TesterNode_be root = tree.getRoot();
		if (root != null) {
			// color of the dot graph
			sColor = Util.getColor();
			testerNH_root = new TesterNodeHead_be(node.getStParent().getIp(),root.getId(), sColor, root
					.getChildL(), root.getChildR(), root.getEquilibre());
			if (TesterNumberCreated < lTesterMax) {
				// create other tester tree with children station
				for (Node stationNode : node.getListChildStation()) {
					if (TesterNumberCreated < lTesterMax) {
						TesterNodeHead_be childTesterHead = createTree(stationNode);
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

	public static long getID_Auto() {
		return ID_auto++;
	}

	public static void setTesterNumberCreated() {
		TesterNumberCreated++;
	}

	public static long getTesterNumberCreated() {
		return TesterNumberCreated;
	}
}
