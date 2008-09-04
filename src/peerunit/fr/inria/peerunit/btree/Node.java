package fr.inria.peerunit.btree;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Verdicts;

public interface Node extends Remote{
	public void send(MessageType message,MethodDescription mdToExecute) throws RemoteException;
	public void setElements(BTreeNode bt,TreeElements te) throws RemoteException;	
	public void sendVerdict(List<Verdicts> localVerdicts) throws RemoteException;
}
