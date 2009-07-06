package fr.inria.peerunit.onstree.testerTree;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface RemoteTesterTreeBuilder extends Remote {
	TesterNodeHead getTesterTreeRoot() throws RemoteException;
	HashMap<String, TesterNodeHead> getIPNodeHeadMap (TesterNodeHead testerNodeHead) throws RemoteException;
}
