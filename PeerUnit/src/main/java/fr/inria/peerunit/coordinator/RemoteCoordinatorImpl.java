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
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.Tester;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author sunye
 */
public class RemoteCoordinatorImpl implements Coordinator, Serializable {

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
     * Counter used to increment tester identifications.
     */
    private final AtomicInteger idCounter = new AtomicInteger(0);

    /**
     * @param nrOfTesters Expected number of testers.
     */
    public RemoteCoordinatorImpl(int nrOfTesters) {
        registrations = new ArrayBlockingQueue<TesterRegistration>(nrOfTesters);
        results = new ArrayBlockingQueue<ResultSet>(nrOfTesters);
        leaving = new ArrayBlockingQueue<Tester>(nrOfTesters);
    }

    /**
     * Test case registration.
     *
     * @param tr TesterRegistration instance.
     * @throws RemoteException
     */
    public void registerMethods(TesterRegistration tr)
            throws RemoteException {

        registrations.offer(tr);
    }

    /**
     * Method execution end. Sent by testers after the execution of a
     * method (TestStep).
     *
     * @param result The result.
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
     *
     * @return An int, the ID for the registering tester.
     * @throws RemoteException
     */
    public int register() throws RemoteException {
        LOG.entering("CoordinatorIml", "register(Tester)");
        return idCounter.getAndIncrement();
    }

    @Override
    public String toString() {
        return String.format("Coordinator for %d testers.",
                this.idCounter.get());
    }

    /**
     * Pending registrations.
     *
     * @return Pending registrations.
     */
    public BlockingQueue<TesterRegistration> registrations() {
        return registrations;
    }

    /**
     * @return Pending test results.
     */
    public BlockingQueue<ResultSet> results() {
        return results;
    }

    /**
     * Testers leaving the system.
     *
     * @return Tester leaving the system.
     */
    public BlockingQueue<Tester> leaving() {
        return leaving;
    }
}