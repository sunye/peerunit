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

import fr.inria.peerunit.base.ResultListenner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Tester;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author sunye
 *
 */
public class CoordinatorImpl implements Runnable {

    private static final Logger LOG = Logger.getLogger(CoordinatorImpl.class.getName());
    private static final long serialVersionUID = 1L;
    /**
     * Schedule: Methods X set of Testers
     */
    private Schedule schedule = new Schedule();
    /**
     * Testers registered to this coordinator
     */
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
    /**
     * The global verdict for a test case.
     */
    private GlobalVerdict verdict;
    /**
     * The coordinator interface, RMI implementation.
     */
    private RemoteCoordinatorImpl remoteCoordinator;
    /**
     * List of result listenners.
     */
    private List<ResultListenner> listenners = new ArrayList<ResultListenner>();
    /**
     * Strategy for test step execution.
     */
    private CoordinationStrategy strategy;

    /**
     * @param testerNbr Number of expected testers. The Coordinator will wait for
     * the connection of "i" testers before starting to dispatch actions
     * to Testers.
     */
    public CoordinatorImpl(int testerNbr) {
        this(testerNbr, 100, new RemoteCoordinatorImpl(testerNbr));
    }

    /**
     *
     * @param testerNbr Number of expected testers.
     * @param relaxIndex
     */
    public CoordinatorImpl(int testerNbr, TesterUtil tu) {
        this(testerNbr, tu.getRelaxIndex(), new RemoteCoordinatorImpl(testerNbr),
                tu.getCoordinationStrategyClass());
    }

    public CoordinatorImpl(int testerNbr, int relaxIndex, Class<?> strategyClass) {
        this(testerNbr, relaxIndex, new RemoteCoordinatorImpl(testerNbr), strategyClass);
    }

    public CoordinatorImpl(int testerNbr, int relaxIndex, RemoteCoordinatorImpl rci) {
        this(testerNbr, relaxIndex, rci, null);
    }

    /**
     *
     * @param testerNbr Number of expected testers.
     * @param relaxIndex
     * @param rci
     */
    private CoordinatorImpl(int testerNbr, int relaxIndex, RemoteCoordinatorImpl rci,
            Class<?> strategyClass) {

        LOG.log(Level.FINEST, "Creating a CoordinatorImpl for {0} testers.", testerNbr);
        

        expectedTesters = new AtomicInteger(testerNbr);
        registeredTesters = Collections.synchronizedList(new ArrayList<Tester>(testerNbr));
        executor = Executors.newFixedThreadPool(testerNbr > 10 ? 10 : testerNbr);
        runningTesters = new AtomicInteger(0);
        verdict = new GlobalVerdict(relaxIndex);
        remoteCoordinator = rci;

        if (strategyClass == null) {
            strategyClass = SequencialStrategy.class;
        }

        LOG.log(Level.FINEST, "Coordination strategy: {0}", strategyClass.getName());
        TesterSet ts = new TesterSetImpl(this);
        try {
            strategy = (CoordinationStrategy) strategyClass.newInstance();
            strategy.init(ts);
        } catch (Exception e) {
            LOG.warning("Error while initializing class: " + strategyClass.getName());
            LOG.log(Level.WARNING, null, e);
            e.printStackTrace();
            strategy = new SequencialStrategy();
            strategy.init(ts);
        }
    }

    /**
     * 
     * @param tu Defaults.
     */
    public CoordinatorImpl(TesterUtil tu) {
        this(tu.getExpectedTesters(), tu.getRelaxIndex(), tu.getCoordinationStrategyClass());
    }

    /**
     * Main thread;
     */
    public void run() {
        LOG.entering(null, "run()");
        try {
            waitForTesterRegistration();
            testcaseExecution();
            quitAllTesters();
            waitAllTestersToQuit();
            printVerdict();
            cleanUp();
        } catch (InterruptedException ie) {
            LOG.warning(ie.getMessage());
        } finally {
            LOG.exiting(null, "run()");
        }
    }

    /**
     * Waits for all testers to quit and calculates the global verdict
     * for a test case.
     * @param chrono
     */
    public void printVerdict() {
        LOG.fine(verdict.toString());
        System.out.println(verdict);
    }

    /**
     * Dispatches test steps to testers:
     *
     * @throws InterruptedException
     */
    public void testcaseExecution() throws InterruptedException {
        LOG.entering("CoordinatorImpl", "testCaseExecution()");
        LOG.finer(String.format("RegistredMethods: %d", schedule.size()));

        strategy.testcaseExecution();

    }

    /**
     * Dispatches a given action to a given set of testers.
     * Waits (blocks) until all tester have executed the action.
     * @param testers
     * @param md
     * @throws InterruptedException
     */
    public void execute(MethodDescription md) throws InterruptedException {
        //assert (status = RUNNING) == RUNNING;
        assert md != null : "Null MethodDescription";

        LOG.entering("CoordinatorImpl", "execute()", md);

        ResultSet result = new ResultSet(md);
        verdict.putResult(md, result);
        result.start();

        Collection<Tester> testers = schedule.testersFor(md);
        String message = String.format("Method %s will be executed by %d testers", md, testers.size());
        LOG.fine(message);
        //System.out.println(message);
        runningTesters.set(testers.size());
        for (Tester each : testers) {
            //LOG.finest("Dispatching " + md + " to tester " + each);
            executor.submit(new MethodExecute(each, md));
        }
        waitForExecutionFinished();
        result.stop();
        LOG.fine("Method " + md + " executed in " + result.getDelay() + " msec");
    }

    public void execute(Integer order) throws InterruptedException {

        LOG.entering("CoordinatorImpl", "execute()", order);

        Collection<MethodDescription> orderMd = schedule.methodsFor(order);

        int count = 0;

        for(MethodDescription md : orderMd) {

            System.out.println("Teste: " + md.getName());

            Collection<Tester> testers = schedule.testersFor(md);

            for (Tester orderTester : testers) {

                executor.submit(new MethodExecute(orderTester, md));

                count++;
            }

        }

       // runningTesters.set(count);

       // waitForExecutionFinished();

        /*
        ResultSet result = new ResultSet(md);
        verdict.putResult(md, result);
        result.start();

        Collection<Tester> testers = schedule.testersFor(md);
        String message = String.format("Method %s will be executed by %d testers", md, testers.size());
        LOG.fine(message);
        //System.out.println(message);
        runningTesters.set(testers.size());
        for (Tester each : testers) {
            //LOG.finest("Dispatching " + md + " to tester " + each);
            executor.submit(new MethodExecute(each, md));
        }
        waitForExecutionFinished();
        result.stop();
        LOG.fine("Method " + md + " executed in " + result.getDelay() + " msec");

        */
    }

    public void setResult(MethodDescription md, ResultSet rs) throws InterruptedException {
        //assert (status = RUNNING) == RUNNING;
        assert md != null : "Null MethodDescription";

        LOG.entering("CoordinatorImpl", "setResult()", md);

        ResultSet result = rs;
        verdict.putResult(md, result);
        result.start();
        
        result.stop();
        LOG.fine("Method " + md + " executed in " + result.getDelay() + " msec");
    }

    public ResultSet getResultFor(MethodDescription md) {
        return verdict.getResultFor(md);
    }

    /**
     * @return The schedule, a read-only map of (Methods X Testers).
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Waits for all expected testers to register their methods (TestSteps).
     */
    public void waitForTesterRegistration() throws InterruptedException {
        LOG.fine("Waiting for registration. Expecting " + expectedTesters + " testers.");

        TesterRegistration reg;

        while (registeredTesters.size() < expectedTesters.intValue()) {
            reg = remoteCoordinator.registrations().take();
            for (MethodDescription m : reg.methods()) {
                schedule.put(m, reg.tester());
            }
            registeredTesters.add(reg.tester());
            LOG.finest("Total tester registrations: " + registeredTesters.size());
        }
        LOG.exiting("CoordinatorImpl", "waitForTesterRegistration()");
    }

    /**
     * Waits for all testers to finish the execution of a method.
     *
     * TODO This code is weird. Verdict needs some refactorings.
     * TODO Timeout needed.
     *
     */
    private void waitForExecutionFinished() throws InterruptedException {
        LOG.fine("Waiting for the end of the execution.");
        while (runningTesters.intValue() > 0) {
            ResultSet rs = remoteCoordinator.results().take();
            ResultSet result = verdict.getResultFor(rs.getMethodDescription());
            result.add(rs);
            for (ResultListenner each : listenners) {
                each.newResult(rs);
            }
            runningTesters.decrementAndGet();
        }
    }


    private void quitAllTesters() {
        LOG.entering("CoordinatorImpl", "quitAllTesters()");
        for (Tester each : registeredTesters) {
            executor.submit(new TesterQuit(each));
        }
        LOG.exiting("CoordinatorImpl", "quitAllTesters()");
    }
    /**
     * Waits for all testers to quit the system.
     *
     * @throws InterruptedException
     *
     * TODO Timeout needed.
     */
    public void waitAllTestersToQuit() throws InterruptedException {
        LOG.fine("Waiting all testers to quit.");
        while (registeredTesters.size() > 0) {
            Tester t = remoteCoordinator.leaving().take();
            registeredTesters.remove(t);
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
        return String.format("Coordinator(expected:%s,registered:%s,running:%s)",
                this.expectedTesters, Integer.valueOf(this.registeredTesters.size()),
                this.runningTesters);
    }

    /**
     * 
     * @return The RemoteCoordinator implementation
     */
    public RemoteCoordinatorImpl getRemoteCoordinator() {
        return remoteCoordinator;
    }

    public void registerResultListenner(ResultListenner listenner) {
        this.listenners.add(listenner);
    }
}
