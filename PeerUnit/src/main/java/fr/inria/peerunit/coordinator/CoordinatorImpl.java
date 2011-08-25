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
import fr.inria.peerunit.remote.Tester;
import fr.inria.peerunit.util.TesterUtil;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sunye
 */
public class CoordinatorImpl implements Runnable {

    private static final Logger LOG = Logger.getLogger(CoordinatorImpl.class.getName());
    /**
     * Schedule: Methods X set of Testers
     */
    private final Schedule schedule = new Schedule();
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
    private final ExecutorService executor;
    /**
     * The global verdict for a test case.
     */
    private final GlobalVerdict verdict;
    /**
     * The coordinator interface, RMI implementation.
     */
    private final RemoteCoordinatorImpl remoteCoordinator;
    /**
     * Strategy for test step execution.
     */
    private CoordinationStrategy strategy;

    /**
     * @param testerNbr Number of expected testers. The Coordinator will wait for
     *                  the connection of "i" testers before starting to dispatch actions
     *                  to Testers.
     */
    public CoordinatorImpl(int testerNbr) {
        this(testerNbr, new RemoteCoordinatorImpl(testerNbr));
    }

    /**
     * @param testerNbr Number of expected testers.
     * @param tu        TesterUtil instance.
     */
    public CoordinatorImpl(int testerNbr, TesterUtil tu) {
        this(testerNbr, new RemoteCoordinatorImpl(testerNbr),
                tu.getCoordinationStrategyClass());
    }

    private CoordinatorImpl(int testerNbr, Class<?> strategyClass) {
        this(testerNbr, new RemoteCoordinatorImpl(testerNbr), strategyClass);
    }

    private CoordinatorImpl(int testerNbr, RemoteCoordinatorImpl rci) {
        this(testerNbr, rci, null);
    }

    /**
     * @param testerNbr     Number of expected testers.
     * @param rci           RemoteCoordinatorImpl instance.
     * @param strategyClass The strategy adopted.
     */
    private CoordinatorImpl(int testerNbr, RemoteCoordinatorImpl rci,
                            Class<?> strategyClass) {

        LOG.log(Level.FINEST, "Creating a CoordinatorImpl for {0} testers.", testerNbr);
        Class<?> strategyClazz;

        expectedTesters = new AtomicInteger(testerNbr);
        registeredTesters = Collections.synchronizedList(new ArrayList<Tester>(testerNbr));
        executor = Executors.newFixedThreadPool(testerNbr > 10 ? 10 : testerNbr);
        runningTesters = new AtomicInteger(0);
        verdict = new GlobalVerdict();
        remoteCoordinator = rci;

        if (strategyClass == null) {
            strategyClazz = GlobalStrategy.class;
        } else {
            strategyClazz = strategyClass;
        }

        LOG.log(Level.FINEST, "Coordination strategy: {0}", strategyClazz.getName());

        /**
         * Scape of this reference during object construction. This can cause bugs.
         * Consider set this somewhere else.
         */
        TesterSet ts = new TesterSetImpl(this);

        try {
            strategy = (CoordinationStrategy) strategyClazz.newInstance();
            strategy.init(ts);
        } catch (Exception e) {
            LOG.warning("Error while initializing class: " + strategyClazz.getName());
            LOG.log(Level.WARNING, null, e);
            e.printStackTrace();
            strategy = new GlobalStrategy();
            strategy.init(ts);
        }
    }

    /**
     * @param tu Defaults.
     */
    public CoordinatorImpl(TesterUtil tu) {
        this(tu.getExpectedTesters(), tu.getCoordinationStrategyClass());
    }

    /**
     * Main thread;
     */
    public void run() {
        LOG.entering(null, "run()");
        try {
            waitForTesterRegistration();
            testCaseExecution();
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
     */
    void printVerdict() {
        LOG.fine(verdict.toString());
        System.out.println(verdict);
    }

    /**
     * Dispatches test steps to testers:
     *
     * @throws InterruptedException Thread exception
     */
    void testCaseExecution() throws InterruptedException {
        LOG.entering("CoordinatorImpl", "testCaseExecution()");
        LOG.finer(String.format("RegisteredMethods: %d", schedule.size()));

        strategy.testCaseExecution();

    }

    /**
     * Dispatches a given action to a given set of testers.
     * Waits (blocks) until all tester have executed the action.
     *
     * @param md Method description.
     * @throws InterruptedException Thread exception.
     */
    public void execute(MethodDescription md) throws InterruptedException {
        assert md != null : "Null MethodDescription";

        LOG.entering("CoordinatorImpl", "execute(MethodDescription)", md);

        ResultSet result = new ResultSet(md);
        verdict.putResult(md, result);
        result.start();

        Collection<Tester> testers = schedule.testersFor(md);
        String message = String.format("Method %s will be executed by %d testers", md, testers.size());
        LOG.fine(message);
        runningTesters.set(testers.size());
        for (Tester each : testers) {
            executor.submit(new MethodExecute(each, md));
        }
        waitForExecutionFinished();
        result.stop();
        LOG.fine("Method " + md + " executed in " + result.getDelay() + " ms");
    }

    /**
     * Dispatches the actions of a given hierarchical level(order).
     * Waits until all tester have executed the actions.
     *
     * @param level The level (order) of an action.
     * @throws InterruptedException Thread exception.
     */
    public void execute(Integer level) throws InterruptedException {

        LOG.entering("CoordinatorImpl", "execute(Integer)", level);

        Map<MethodDescription, AtomicInteger> testersMap =
                Collections.synchronizedMap(new TreeMap<MethodDescription, AtomicInteger>());

        Map<MethodDescription, ResultSet> resultMap =
                Collections.synchronizedMap(new TreeMap<MethodDescription, ResultSet>());

        for (MethodDescription action : schedule.methodsFor(level)) {
            ResultSet result = new ResultSet(action);
            verdict.putResult(action, result);
            result.start();

            Collection<Tester> testers = schedule.testersFor(action);
            LOG.log(Level.FINE, "Method {0} will be executed by {1} testers", new Object[]{action, testers.size()});
            runningTesters.set(runningTesters.get() + testers.size());
            for (Tester tester : testers) {
                executor.submit(new MethodExecute(tester, action));
            }
            testersMap.put(action, new AtomicInteger(testers.size()));
            resultMap.put(action, result);
        }

        while (runningTesters.intValue() > 0) {
            ResultSet rs = remoteCoordinator.results().take();
            verdict.getResultFor(rs.getMethodDescription()).add(rs);
            runningTesters.decrementAndGet();
            if (testersMap.get(rs.getMethodDescription()).decrementAndGet() == 0) {
                ResultSet result = resultMap.get(rs.getMethodDescription());
                result.stop();
                LOG.fine("Method " + result.getMethodDescription() + " executed in "
                        + result.getDelay() + " ms");
            }
        }
    }

    public ArrayList<String> execute(Integer order, TesterSet testerSet, ArrayList<String> errors) throws InterruptedException {

        LOG.entering("CoordinatorImpl", "executeGlobal()", order);

        boolean error = false;

        ArrayList<Tester> testersExecuting = new ArrayList<Tester>();

        Collection<MethodDescription> orderMd = schedule.methodsFor(order);

        ArrayList<Object> result = new ArrayList<Object>();

        for (MethodDescription action : orderMd) {

            ResultSet res = new ResultSet(action);
            result.add(res);
            verdict.putResult(action, res);
            res.start();

            error = false;
            for (String depend : action.getDepends()) {
                if (errors.contains(depend)) {
                    error = true;

                    //  res = new ResultSet(action);
                    res.addSimulatedError();

                    testerSet.setResult(action, res);
                    errors.add(action.getName());
                    LOG.log(Level.FINEST, "Action {0} was not executed due to its dependence!", action.getName());
                    res.stop();

                    break;
                }
            }

            if (!error) {

                Collection<Tester> testers = schedule.testersFor(action);

                String message = String.format("Method %s will be executed by %d testers", action, testers.size());
                LOG.fine(message);

                testersExecuting.clear();

                for (Tester orderTester : testers) {

                    executor.submit(new MethodExecute(orderTester, action));

                    if (!testersExecuting.contains(orderTester)) {

                        testersExecuting.add(orderTester);

                    }

                }
            }

        }

        // Stopping result sets
        if (!error) {

            for (Object r : result) {
                // Casting
                ResultSet rs = (ResultSet) r;

                // Set the testers executing number.
                runningTesters.set(schedule.testersFor(rs.getMethodDescription()).size());

                // Waiting for executions finish
                waitForExecutionFinished();

                rs.stop();

                // Errors
                if (rs.getErrors() > 0
                        || rs.getInconclusive() > 0
                        || rs.getFailures() > 0) {
                    errors.add(rs.getMethodDescription().getName());
                }

                LOG.fine("Method " + rs.getMethodDescription() + " executed in " + rs.getDelay() + " ms");

            }

        }

        return (errors);

    }

    public void setResult(MethodDescription md, ResultSet rs) {
        //assert (status = RUNNING) == RUNNING;
        assert md != null : "Null MethodDescription";

        LOG.entering("CoordinatorImpl", "setResult()", md);

        verdict.putResult(md, rs);
        rs.start();

        rs.stop();
        LOG.fine("Method " + md + " executed in " + rs.getDelay() + " ms");
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
     *
     * @throws InterruptedException Thread exception
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
     * @throws InterruptedException Thread exception
     */
    private void waitForExecutionFinished() throws InterruptedException {
        LOG.fine("Waiting for the end of the execution.");

        while (runningTesters.intValue() > 0) {

            ResultSet rs = remoteCoordinator.results().take();

            ResultSet result = verdict.getResultFor(rs.getMethodDescription());
            result.add(rs);

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
     * @throws InterruptedException Thread exception.
     */
    void waitAllTestersToQuit() throws InterruptedException {
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
    void cleanUp() {
        LOG.fine("Coordinator cleaning up.");
        schedule.clear();
        runningTesters.set(0);
        registeredTesters.clear();
        executor.shutdown();
    }

    @Override
    public String toString() {
        return String.format("Coordinator(expected:%s,registered:%s,running:%s)",
                expectedTesters, registeredTesters.size(), runningTesters);
    }

    /**
     * @return The RemoteCoordinator implementation
     */
    public RemoteCoordinatorImpl getRemoteCoordinator() {
        return remoteCoordinator;
    }
}