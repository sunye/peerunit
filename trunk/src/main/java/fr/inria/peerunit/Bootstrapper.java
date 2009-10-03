/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author sunye
 */
public interface Bootstrapper extends Remote {
	/**
	 * Registers a tester to this Bootstrapper
	 *
	 * @param t
	 * @return the generated ID for the added node, or Integer.MAX_VALUE
         * if all nodes have already been registered
	 * @throws java.rmi.RemoteException
	 */
	public int register(Tester t) throws RemoteException;


	/**
	 * Return true if id follow to Bootstrapper
	 * @param id
	 * @return
	 * @throws RemoteException
	 */
	public boolean isRoot(int id) throws RemoteException;
}

