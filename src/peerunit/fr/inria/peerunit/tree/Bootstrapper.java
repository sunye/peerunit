package fr.inria.peerunit.tree;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bootstrapper extends Remote{
	public int register(TreeTester t) throws RemoteException;	
	
}