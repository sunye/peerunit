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
package fr.inria.peerunit.distributed;

import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.coordinator.RemoteCoordinatorImpl;
import fr.inria.peerunit.coordinator.Schedule;
import fr.inria.peerunit.coordinator.TesterRegistration;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.Tester;
import fr.inria.peerunit.tester.RemoteTesterImpl;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class MiddleTester {

    private static Logger LOG = Logger.getLogger(MiddleTester.class.getName());

    /**
     * Coordinator of this tester.
     */
    private Coordinator parent;
     /**
     * Testers registered to this coordinator
     */
    private final List<Tester> children;

    /**
     * Tester interface, RMI implementation.
     * Used to register with parent (Coordinator), which thinks this is a
     * single Tester.
     */
    private final RemoteTesterImpl tester;

    /**
     * Coordinator interface, RMI implementation.
     * Used to communicate with the childre (Testers), which think this is a
     * coordinator.
     */
    private final RemoteCoordinatorImpl coordinator;

    /**
     * Remaining tester registrations.
     */
    private final AtomicInteger remainingRegistrations;

    /**
     * Number of expected testers.
     */
    private final int expectedTesters;

    /**
     * Schedule (Methods X Testers)
     */
    private Schedule schedule = new Schedule();


    /**
     * Number of running testers in a given moment.
     */
    private AtomicInteger runningTesters = new AtomicInteger(0);

    /**
     * Result set for a method execution.
     */
    private ResultSet executionResult;
   
    public MiddleTester(int testerNbr) {
        tester = new RemoteTesterImpl();
        coordinator = new RemoteCoordinatorImpl(testerNbr);
        expectedTesters = testerNbr;
        remainingRegistrations = new AtomicInteger(testerNbr);
        children = Collections.synchronizedList(new ArrayList<Tester>(testerNbr));
    }

    /**
     * Accessor to the tester interface
     * @return the Tester interface
     */
    public Tester getTester() {
        return tester;
    }

    /**
     * Accessor to the coordinator interface
     * @return the Coordinator interface
     */
    public Coordinator getCoordinator() {
        return coordinator;
    }

    /**
     * MiddleTester main execution behavior, which mixes coordination and
     * testers.
     * @throws InterruptedException
     * @throws RemoteException
     */
    public void execute() throws InterruptedException, RemoteException {
        LOG.entering("ManInTheMiddle", "execute()");
        this.waitForTesterRegistration();
        this.registerWithParent();
        this.testCaseExecution();
        this.waitAllTestersToQuit();
        LOG.exiting("ManInTheMiddle", "execute()");
    }


    /**
     * Waits for all expected testers to register their methods (TestSteps).
     */
    private void waitForTesterRegistration() throws InterruptedException {
        LOG.entering("ManInTheMiddle", "execute()");
        LOG.fine("Waiting for registration. Expecting " + expectedTesters + " testers.");

        TesterRegistration reg;

        while (remainingRegistrations.getAndDecrement() > 0 ) {
            reg = coordinator.registrations().take();
            schedule.put(reg);
            children.add(reg.tester());
            LOG.finest("Remaining registrations: " + remainingRegistrations.get());
        }
        LOG.exiting("ManInTheMiddle", "waitForTesterRegistration()");
    }

    /**
     * Registers methods with parent tester. The registerd methods is a Set,
     * union of all children methods.
     * For instance, if child 1 must execute 2 methods (a, b) and child 2
     * must execute methods (a, c), methods (a, b, c) will be registered.
     * 
     * @throws InterruptedException
     * @throws RemoteException
     */
    private void registerWithParent() throws InterruptedException, RemoteException {
        parent = tester.takeCoordinator();
        parent.registerMethods(new TesterRegistration(tester, schedule.methods()));
    }

    /**
     * Waits for execution messages from parent and dispatches to children.
     * 
     * @throws InterruptedException
     * @throws RemoteException
     */
    private void testCaseExecution() throws InterruptedException, RemoteException {
        LOG.entering("ManInTheMiddle", "testCaseExecution()");
        
        Collection<MethodDescription> remainingMethods = schedule.methods();

        while (remainingMethods.size() > 0) {
            MethodDescription md = tester.takeMethodDescription();
            this.methodExecution(md);
            this.waitForExecutionFinished();
            remainingMethods.remove(md);
        }
        LOG.exiting("ManInTheMiddle", "testCaseExecution()");
    }

    /**
     * Dispatches a execution message to all concerned children.
     * 
     * @param md
     * @throws RemoteException
     */
    private void methodExecution(MethodDescription md) throws RemoteException {
        LOG.entering("ManInTheMiddle", "methodExecution()");
        executionResult = new ResultSet(md);
        Collection<Tester> testers = schedule.testersFor(md);
        String message = String.format("Method %s will be executed by %d testers", md, testers.size());
        LOG.fine(message);
        runningTesters.set(testers.size());
        for (Tester each : testers) {
            each.execute(md);
        }
        LOG.exiting("ManInTheMiddle", "methodExecution()");
   }


    /**
     * Waits for a response from all children that executed a method.
     *
     * @throws InterruptedException
     * @throws RemoteException
     */
    private void waitForExecutionFinished() throws InterruptedException, RemoteException {
        LOG.entering("ManInTheMiddle", "waitForExecutionFinished()");
        
        while (runningTesters.intValue() > 0) {
            ResultSet rs = coordinator.results().take();
            executionResult.add(rs);
            runningTesters.decrementAndGet();
        }
        parent.methodExecutionFinished(executionResult);
        LOG.exiting("ManInTheMiddle", "waitForExecutionFinished()");

    }

    /**
     * Waits for all children to quit and then quits with parent.
     * 
     * @throws InterruptedException
     * @throws RemoteException
     */
    private void waitAllTestersToQuit() throws InterruptedException, RemoteException {
        LOG.entering("ManInTheMiddle", "waitAllTestersToQuit()");
        while (children.size() > 0) {
            Tester t = coordinator.leaving().take();
            children.remove(t);
            LOG.fine(String.format("Waiting for %d testers to quit.", children.size()));
        }

        parent.quit(tester);
        LOG.exiting("ManInTheMiddle", "waitAllTestersToQuit()");

    }
}
