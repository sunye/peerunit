package fr.inria.peerunit.tree;

import java.rmi.RemoteException;

public interface Bootstrapper {

	public abstract void register(TreeTester t) throws RemoteException;
	public  int getNewId(TreeTester t) throws RemoteException ;

}