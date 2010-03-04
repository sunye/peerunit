/*
This file is part of PeerUnit.

PeerUnit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PeerUnit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.coordinator;

import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.Tester;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class RemoteCoordinatorImpl implements Bootstrapper, Coordinator {

    private static final Logger LOG = Logger.getLogger(RemoteCoordinatorImpl.class.getName());

    /**
     * Stores arriving registrations.
     */
    private final BlockingQueue<TesterRegistration> registrations;

    /**
     * Stores method execution results.
     */
    private final BlockingQueue<ResultSet> results;

    /**
     * Stores testers that are leaving the system.
     */
    private final BlockingQueue<Tester> leaving;

    /**
     * Quantity of registered testers.
     */
    private final AtomicInteger registerdTesters = new AtomicInteger(0);

    /**
     *
     * @param expectedTesters Expected number of testers.
     */
    public RemoteCoordinatorImpl(int expectedTesters) {
        registrations = new ArrayBlockingQueue<TesterRegistration>(expectedTesters);
        results = new ArrayBlockingQueue<ResultSet>(expectedTesters);
        leaving = new ArrayBlockingQueue<Tester>(expectedTesters);
    }

    /**
     * Bulk tester registration.
     * Not implemented yet.
     * @param testers
     * @throws RemoteException
     */
    public void registerTesters(List<Tester> testers) throws RemoteException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Test case registration.
     *
     * @param t
     * @param coll
     * @throws RemoteException
     */
    public void registerMethods(Tester t, Collection<MethodDescription> coll)
            throws RemoteException {
        registrations.offer(new TesterRegistration(t, coll));
    }

    /**
     * Method execution end. Sent by testers after the execution of a
     * method (TestStep).
     * @param result
     * @throws RemoteException
     */
    public void methodExecutionFinished(ResultSet result) throws RemoteException {
        results.offer(result);
    }

    /**
     * Informs the coordinator that a tester is leaving the system.
     * 
     * @param t The tester.
     * @throws RemoteException
     */
    public void quit(Tester t) throws RemoteException {
        leaving.offer(t);
    }

    /**
     * Bootstrapper method.
     * Registers a tester.
     * @param t
     * @return An int, the ID for the registering tester.
     * @throws RemoteException
     */
    public int register(Tester t) throws RemoteException {
        LOG.entering("CoordinatorIml", "register(Tester)");
        int id = registerdTesters.getAndIncrement();
        LOG.fine("New Registered Tester: " + id + " new client " + t);
        return id;
    }

    /**
     * Forces the coordinator to quit.
     * @throws RemoteException
     */
    public void quit() throws RemoteException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Pending registrations.
     *
     * @return
     */
    BlockingQueue<TesterRegistration> registrations() {
        return registrations;
    }

    /**
     * Pending test results.
     * @return
     */
    BlockingQueue<ResultSet> results() {
        return results;
    }

    /**
     * Testers leaving the system.
     * @return
     */
    BlockingQueue<Tester> leaving() {
        return leaving;
    }
}
