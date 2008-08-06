package fr.inria.peerunit.tree;

import java.rmi.Remote;
import java.rmi.RemoteException;

import fr.inria.peerunit.tree.oldbtree.TreeElements;



public interface TreeTester extends Remote{
	public void setTreeElements(TreeElements tree) throws RemoteException;
	public void startExecution() throws RemoteException;
}
