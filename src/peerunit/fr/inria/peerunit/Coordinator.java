package fr.inria.peerunit;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Verdicts;

public interface Coordinator extends Remote {
	/**
	 * @param tester
	 * @param list
	 * @throws RemoteException
	 */
	public void register(Tester tester, List<MethodDescription> list) throws RemoteException;

	public int getNewId(Tester t) throws RemoteException;
	public void greenLight() throws RemoteException;

	/**
	 * Finish the test case and calculates the global oracle
	 * @param Tester
	 * @param error that informs if the test was finish by error
	 * @param Verdict
	 * @param Expected index of inconclusive verdicts
	 */
	public void quit(Tester t,boolean error,Verdicts v) throws RemoteException;

	/**
	 * Used to cache testing global variables
	 * @param key
	 * @param object
	 * @throws RemoteException
	 */
	public void put(Integer key,Object object)  throws RemoteException;

	/**
	 *  Used to retrieve testing global variables
	 * @param key
	 * @return Object
	 * @throws RemoteException
	 */
	public Object get(Integer key)  throws RemoteException ;
	/**
	 * Used to retrieve all the keys of the testing global variables
	 * @param key
	 * @return Object
	 * @throws RemoteException
	 */
	public  Map<Integer,Object> getCollection()  throws RemoteException ;

	public boolean containsKey(Object key) throws RemoteException ;

	public void clearCollection()throws RemoteException ;
}
