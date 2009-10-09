package fr.inria.peerunit.btreeStrategy;

import java.rmi.RemoteException;

import fr.inria.peerunit.Tester;
import fr.inria.peerunit.util.BTreeNode;

/**
 * The classes that implement a concrete strategy should implement this The
 * context class uses this to call the concrete strategy
 * 
 * @author Veronique Pelleau
 * @author Jeremy Masson
 * @author Aboubakar Koïta
 * 
 */
public interface TreeStrategy {
	public void buildTree();

	public BTreeNode getNode(Integer key); // XXX

	public int getNodesSize();

	public int register(Tester t) throws RemoteException;

	void setCommunication();

	public int getRegistered();

	/**
	 * Blocks current thread until all expected testers have registered.
	 */
	public void waitForTesterRegistration() throws InterruptedException;

        public void startRoot() throws RemoteException;
}
