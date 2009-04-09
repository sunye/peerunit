package fr.inria.peerunit;

import java.rmi.RemoteException;
import java.util.List;

import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Verdicts;

public interface Coordinator extends Architecture {
	/**
	 * @param tester
	 * @param list
	 * @throws RemoteException
	 * @author Eduardo Almeida, Veronique Pelleau
	 */
	public void register(Tester tester, List<MethodDescription> list) throws RemoteException;

	public int getNewId(Tester t) throws RemoteException;

	public void executionFinished() throws RemoteException;

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
