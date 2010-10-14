/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.freepastrytest.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 *
 * @author sunye
 */
public interface RemoteModel extends Remote {

    public void newNode(String id) throws RemoteException;

    public void updateNode(String id, Set<String> neighbors)
            throws RemoteException;
}
