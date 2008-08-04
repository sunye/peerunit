package fr.inria.peerunit.tree;

import java.rmi.Remote;


public interface TreeTester extends Remote, Runnable{
	public void startNet(TreeTester t);
}
