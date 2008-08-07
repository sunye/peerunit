package fr.inria.peerunit.tree;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TreeTester extends Remote{
	public void setTreeElements(TreeElements tree,boolean isRoot) throws RemoteException;
	public void setChildren(TreeTester tester) throws RemoteException; 
	public void startExecution() throws RemoteException;	
}
