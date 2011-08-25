/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @author sunye
 */
public interface DistributedTester extends Remote {

    /**
     * Register several (child) testers.
     *
     * @param testers Distributed testers.
     * @throws RemoteException Remote exception.
     */
    void registerTesters(List<DistributedTester> testers)
            throws RemoteException;

    void setParent(DistributedTester parent) throws RemoteException;

    void start() throws RemoteException;

    void setCoordinator(Coordinator c) throws RemoteException;

    int getId() throws RemoteException;

    String getAddress() throws RemoteException;
}
