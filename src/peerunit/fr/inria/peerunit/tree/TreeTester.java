package fr.inria.peerunit.tree;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import fr.inria.peerunit.test.oracle.Verdicts;

public interface TreeTester extends Remote{
	public void setTreeElements(TreeElements tree,boolean isRoot) throws RemoteException;
	public void setChildren(TreeTester tester) throws RemoteException; 
	public void startExecution() throws RemoteException;
	public void endExecution(List<Verdicts> localVerdicts) throws RemoteException;
	public int getId() throws RemoteException;
}
