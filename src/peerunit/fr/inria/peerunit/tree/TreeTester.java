package fr.inria.peerunit.tree;

import java.rmi.Remote;
import java.rmi.RemoteException;

import fr.inria.peerunit.test.oracle.Verdicts;

public interface TreeTester extends Remote{
	public void setTreeElements(TreeElements tree,boolean isRoot) throws RemoteException;
	public void setChildren(TreeTester tester) throws RemoteException; 
	public void startExecution() throws RemoteException;
	public void endExecution(Verdicts v) throws RemoteException;
	public int getId() throws RemoteException;
}
