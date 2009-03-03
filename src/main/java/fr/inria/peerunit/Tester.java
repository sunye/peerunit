package fr.inria.peerunit;


import java.rmi.Remote;
import java.rmi.RemoteException;

import fr.inria.peerunit.parser.MethodDescription;


public interface Tester extends Remote {
	public void execute(MethodDescription m) throws RemoteException;
	public int getPeerName() throws RemoteException;
}
