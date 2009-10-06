package fr.inria.peerunit;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Verdicts;
import java.rmi.Remote;

public interface Coordinator extends Remote {
	
	
	public void registerTesters(List<Tester> testers) throws RemoteException;
	
	
	/**
	 * @param tester
	 * @param list
	 * @throws RemoteException
	 * @author Eduardo Almeida, Veronique Pelleau
	 */
	public void registerMethods(Tester tester, Collection<MethodDescription> list)
			throws RemoteException;

	public void methodExecutionFinished(Tester tester, MessageType message)
			throws RemoteException;

	/**
	 * Finish the test case and calculates the global oracle
	 * 
	 * @param Tester
	 * @param Verdict
	 * 
	 */
	public void quit(Tester t, Verdicts v) throws RemoteException;
	
}
