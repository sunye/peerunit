package fr.inria.peerunit.tree;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import fr.inria.peerunit.test.oracle.Verdicts;

/**
 * This tester interface used to build the tree tester, execute the actions and get the verdict
 * of the actions executed
 * @author Eduardo
 */
public interface TreeTester extends Remote{
	
	/** link tester on the tree node
	 * @param tree
	 * @param isRoot
	 * @throws RemoteException
	 */
	public void setTreeElements(TreeElements tree,boolean isRoot) throws RemoteException;
	
	/** Add the child tester to ElementsTester (node) and remove old child tester
	 * @param child tester
	 * @param old child tester
	 * @throws RemoteException
	 */
	public void setChildren(TreeTester child,TreeTester tester) throws RemoteException;	
	
	/** Ready to execute the test (action).
	 * Going way down the tree
	 * @throws RemoteException
	 */
	public void startExecution() throws RemoteException;
	
	/**
	 * Going way up the verdict of the action executed (test)
	 * @param localVerdicts
	 * @throws RemoteException
	 */
	public void endExecution(List<Verdicts> localVerdicts) throws RemoteException;
	
	/** Return id of the tester 
	 * @return id
	 * @throws RemoteException
	 */
	public int getId() throws RemoteException;
	
	/** Define the parent tester
	 * @param parent
	 * @throws RemoteException
	 */
	public void setParent(TreeTester parent)throws RemoteException;
}
