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
package fr.inria.peerunit.rmi.coord;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.Bootstrapper;
import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.base.Schedule;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.GlobalVerdict;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author sunye
 *
 */
public class CoordinatorImpl implements Coordinator, Bootstrapper,
        Runnable, Serializable {
    
    private static final Logger LOG = Logger.getLogger(CoordinatorImpl.class.getName());

    private static final long serialVersionUID = 1L;
    private static final int STARTING = 0;
    private static final int IDLE = 1;
    private static final int RUNNING = 2;
    private static final int LEAVING = 3;

    private int status = STARTING;

    /**
     * Schedule: Methods X set of Testers
     */
    private Schedule schedule = new Schedule();


    final private List<Tester> registeredTesters;
    /**
     * Number of expected testers.
     */
    final private AtomicInteger expectedTesters;
    /**
     * Number of testers running the current method (test step).
     */
    final private AtomicInteger runningTesters;
    /**
     * Pool of threads. Used to dispatch actions to testers.
     */
    private ExecutorService executor;

    private GlobalVerdict verdict;


    /**
     * @param i Number of expected testers. The Coordinator will wait for
     * the connection of "i" testers before starting to dispatch actions
     * to Testers.
     */
    public CoordinatorImpl(int testerNbr, int relaxIndex) {
        expectedTesters = new AtomicInteger(testerNbr);
        runningTesters = new AtomicInteger(0);
        registeredTesters = Collections.synchronizedList(new ArrayList<Tester>(testerNbr));
        executor = Executors.newFixedThreadPool(testerNbr > 10 ? 10 : testerNbr);
        verdict = new GlobalVerdict(relaxIndex);
    }

    public CoordinatorImpl(TesterUtil tu) {
        this(tu.getExpectedTesters(), tu.getRelaxIndex());
    }

    public void registerTesters(List<Tester> testers) throws RemoteException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @see fr.inria.peerunit.Coordinator#registerMethods(fr.inria.peerunit.Tester,
     *      java.util.List)
     */
    public synchronized void registerMethods(Tester t, Collection<MethodDescription> list)
            throws RemoteException {

        assert status == STARTING : "Trying to register while not starting";
        LOG.entering("CoordinatorImpl", "registerMethods(Tester, Collection)");


        if (registeredTesters.size() >= expectedTesters.intValue()) {
            LOG.warning("More registrations than expected");
            return;
        }

        for (MethodDescription m : list) {
            schedule.put(m, t);
        }
        registeredTesters.add(t);
        synchronized (registeredTesters) {
            registeredTesters.notifyAll();
        }
    }

    public void run() {
        try {
            waitForTesterRegistration();
            testcaseExecution();
            waitAllTestersToQuit();
            printVerdict();
            cleanUp();
        } catch (InterruptedException ie) {
            LOG.warning(ie.getMessage());
        }
    }

    /**
     * Waits for all testers to quit and calculates the global verdict
     * for a test case.
     * @param chrono
     */
    public void printVerdict() {
        System.out.println(verdict);
    }

    /**
     * Dispatches actions to testers:
     *
     * @param chrono
     * @throws InterruptedException
     */
    public void testcaseExecution() throws InterruptedException {
        assert (status == IDLE) : "Trying to execute test case while not idle";

        LOG.entering("CoordinatorImpl", "testCaseExecution()");
        LOG.finer(String.format("RegistredMethods: %d", schedule.size()));

        for (MethodDescription each : schedule.methods()) {
            execute(each);
        }
        
        assert (status = LEAVING) == LEAVING;
    }

    /**
     * Dispatches a given action to a given set of testers.
     * Waits (blocks) until all tester have executed the action.
     * @param testers
     * @param md
     * @throws InterruptedException
     */
    public void execute(MethodDescription md) throws InterruptedException {
        assert (status = RUNNING) == RUNNING;
        assert md != null : "Null MethodDescription";

        LOG.entering("CoordinatorImpl", "execute()",md);
        ResultSet result = new ResultSet(md);
        verdict.putResult(md, result);
        result.start();

        Collection<Tester> testers = schedule.testersFor(md);
        String message = String.format("Method %s will be executed by %d testers", md, testers.size());
        LOG.fine(message);
        //System.out.println(message);
        runningTesters.set(testers.size());
        for (Tester each : testers) {
            LOG.finest("Dispatching " + md + " to tester " + each);
            executor.submit(new MethodExecute(each, md));
        }
        waitForExecutionFinished();
        result.stop();
        LOG.finest("Method " + md + " executed in " + result.getDelay() + " msec");
    }

    public ResultSet getResultFor(MethodDescription md) {
        return verdict.getResultFor(md);
    }


    /*
     * (non-Javadoc)
     *
     * @see callback.Coordinator#namer(callback.Tester) Incremented with
     * java.util.concurrent to handle the semaphore concurrency access
     */
    public synchronized int register(Tester t) throws RemoteException {
        LOG.entering("CoordinatorIml", "register(Tester)");
        int id = runningTesters.getAndIncrement();
        LOG.fine("New Registered Tester: " + id + " new client " + t);
        return id;
    }

    public synchronized void methodExecutionFinished(ResultSet rs) throws RemoteException {
//        assert status == RUNNING : "Trying to finish before execution";
        assert rs.getMethodDescription() != null;
        assert verdict.containsMethod(rs.getMethodDescription()) : "Execution finished for an unknwon method";

        ResultSet result = verdict.getResultFor(rs.getMethodDescription());
        result.add(rs);
        runningTesters.decrementAndGet();

        synchronized (runningTesters) {
            runningTesters.notifyAll();
        }
    }

    public synchronized void quit(Tester t) throws RemoteException {
        //assert status == LEAVING : "Trying to quit during execution";
        LOG.fine(String.format("Tester %s is leaving.",t));
        
        synchronized (registeredTesters) {
            registeredTesters.remove(t);
            registeredTesters.notifyAll();
        }
    }

    /**
     * @return A read-only map of (Methods X Testers).
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Waits for all expected testers to registerMethods.
     */
    public void waitForTesterRegistration() throws InterruptedException {
        assert status == STARTING : "Trying to register while not starting";

        LOG.fine("Waiting for registration. Expecting " + expectedTesters + " testers.");
        while (registeredTesters.size() < expectedTesters.intValue()) {
            synchronized (registeredTesters) {
                registeredTesters.wait();
            }
        }

        assert (status = IDLE) == IDLE;
    }

    /**
     * Waits for all testers to finish the execution of a method.
     */
    private void waitForExecutionFinished() throws InterruptedException {
        assert status == RUNNING : "Trying to finish method while not running";

        LOG.fine("Waiting for the end of the execution.");
        while (runningTesters.intValue() > 0) {
            synchronized (runningTesters) {
                runningTesters.wait();
            }
        }

        assert (status = IDLE) == IDLE;
    }

    /**
     * Waits for all testers to quit the system.
     *
     * @throws InterruptedException
     */
    public void waitAllTestersToQuit() throws InterruptedException {
        //assert status == LEAVING : "Trying to quit before time";

        LOG.fine("Waiting all testers to quit.");
        while (registeredTesters.size() > 0) {
            synchronized (registeredTesters) {
                registeredTesters.wait();
            }
            LOG.fine(String.format("Waiting for %d testers to quit.", registeredTesters.size()));
        }
        LOG.fine("All testers quit.");
    }

    /**
     * Clears references to testers.
     */
    public void cleanUp() {
        LOG.fine("Coordinator cleaning up.");
        schedule.clear();
        runningTesters.set(0);
        registeredTesters.clear();
        executor.shutdown();
    }

    @Override
    public String toString() {
        return String.format("Coordinator(expected:%s,registered:%s,running:%s)", this.expectedTesters, new Integer(this.registeredTesters.size()), this.runningTesters);
    }

    public boolean isRoot(int id) throws RemoteException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void quit() throws RemoteException {
        throw new UnsupportedOperationException("Not supported.");
    }
}
