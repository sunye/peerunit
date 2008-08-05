package fr.inria.peerunit.tree;

import java.rmi.Remote;
import java.rmi.RemoteException;



public interface TreeTester extends Remote{
	public void startNet() throws RemoteException;
}
