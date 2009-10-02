package fr.inria.peerunit;

import java.rmi.RemoteException;
import java.util.List;

import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Verdicts;
import java.rmi.Remote;

public interface Coordinator extends Remote {
	/**
	 * @param tester
	 * @param list
	 * @throws RemoteException
	 * @author Eduardo Almeida, Veronique Pelleau
	 */
	public void registerMethods(Tester tester, List<MethodDescription> list) throws RemoteException;

	public int register(Tester t) throws RemoteException;

	public void methodExecutionFinished() throws RemoteException;

	/**
	 * Finish the test case and calculates the global oracle
	 * @param Tester
	 * @param error that informs if the test was finish by error
	 * @param Verdict
	 * @param Expected index of inconclusive verdicts
	 */
	//public void quit(Tester t,boolean error,Verdicts v) throws RemoteException;
	public void quit(Tester t,Verdicts v) throws RemoteException;
}
