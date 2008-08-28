package fr.inria.peerunit.btree;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote{
	public void send(MessageType message) throws RemoteException;
	public void setElements(BTreeNode bt,TreeElements te) throws RemoteException;
	public void setParent(Node parent)throws RemoteException;
}
