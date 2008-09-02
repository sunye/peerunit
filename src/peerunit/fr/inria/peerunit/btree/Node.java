package fr.inria.peerunit.btree;

import java.rmi.Remote;
import java.rmi.RemoteException;

import fr.inria.peerunit.parser.MethodDescription;

public interface Node extends Remote{
	public void send(MessageType message,MethodDescription mdToExecute) throws RemoteException;
	public void setElements(BTreeNode bt,TreeElements te) throws RemoteException;	
}
