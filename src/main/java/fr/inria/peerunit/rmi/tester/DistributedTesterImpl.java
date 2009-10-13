/*
    This file is part of PeerUnit.

    Foobar is free software: you can redistribute it and/or modify
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
package fr.inria.peerunit.rmi.tester;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Bootstrapper;
import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.MessageType;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.base.AbstractTester;
import fr.inria.peerunit.base.Result;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.rmi.coord.Chronometer;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.TesterUtil;

/**
 * The DistributedTester is both, a Tester and a Coordinator.
 * As a Tester, it has a Coordinator, it registers a test case and executes 
 * test steps when requested by its Coordinator.
 * 
 * As a coordinator, it accepts the registration of several testers
 * and asks its testers to execute test steps.
 * 
 * @author sunye
 */
public class DistributedTesterImpl extends AbstractTester implements Tester, Coordinator, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2806863880157215029L;
    private static Logger LOG = Logger.getLogger(TesterImpl.class.getName());
    /**
     * The coordinator of this tester. Since DistributedTester is used in
     * a distributed architecture, the coordinator is also a DistributedTester.
     *
     */
    private transient Coordinator parent;
    /**
     * Set of testers that are coordinated by this tester.
     */
    private transient List<Tester> testers = new LinkedList<Tester>();
    /**
     * The bootstrapper, that will help me to find my parent and my children.
     */
    private transient Bootstrapper bootstrapper;
    private transient TesterImpl tester;
    private transient CoordinatorImpl coordinator;
    private transient TesterUtil defaults;
    private transient Class<? extends TestCaseImpl> testCaseClass;

    public DistributedTesterImpl(Class<? extends TestCaseImpl> klass, Bootstrapper boot, GlobalVariables gv, TesterUtil tu) throws RemoteException {
        super(gv);
        defaults = tu;
        bootstrapper = boot;
        testCaseClass = klass;
    }

    /**
     * Registers this distributed tester with the bootstrapper and
     * receives an id.
     * 
     * 
     * 
     */
    public void register() {
        LOG.entering("DistributedTester", "register()");

        try {
            this.setId(bootstrapper.register(this));

        } catch (RemoteException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

//        this.tester = new TesterImpl(this.globalTable(), this.getId(), defaults);
//        tester.registerTestCase(testCaseClass);
//        this.testers.add(tester);
    }

    /**
     * Sets the testers that are controlled by this tester and
     * informs the tester that this tester is their controller
     *
     * @see fr.inria.peerunit.Coordinator#registerTesters(java.util.List)
     */
    public void registerTesters(List<Tester> l) throws RemoteException {
        assert l != null : "Null argument";
        assert !l.isEmpty() : "Empty argument";

        LOG.entering("DistributedTesterImpl", "registerTesters(List<Tester>)", l.size());
        testers.addAll(l);

    }

    /**
     * Starts the Coordinator, which will control my children.
     */
    private void createLocalCoordinator() {
        LOG.entering("DistributedTester", "startCoordination()");

        this.coordinator = new CoordinatorImpl(testers.size(), defaults.getRelaxIndex());
    }

    /** 
     * @see fr.inria.peerunit.Coordinator#registerMethods(fr.inria.peerunit.Tester, java.util.List)
     */
    public void registerMethods(Tester tester, Collection<MethodDescription> list) throws RemoteException {
        assert testers.contains(tester);

        coordinator.registerMethods(tester, list);

    }

    /**
     * @throws InterruptedException
     * @see fr.inria.peerunit.Tester#execute(fr.inria.peerunit.parser.MethodDescription)
     */
    public void execute(MethodDescription md) throws RemoteException {

        try {
            coordinator.execute(md);

            // TODO retrieve information about method execution,
            // before sending a OK to parent !

            parent.methodExecutionFinished(null);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    /**
     * @see fr.inria.peerunit.Coordinator#methodExecutionFinished(Tester, fr.inria.peerunit.MessageType)
     */
    public void methodExecutionFinished(Result result) throws RemoteException {
        assert coordinator != null : "Null Coordinator";

        coordinator.methodExecutionFinished(result);
    }

    /** 
     * @see fr.inria.peerunit.Coordinator#quit(fr.inria.peerunit.Tester, fr.inria.peerunit.test.oracle.Verdicts)
     */
    public void quit(Tester t, Verdicts v) throws RemoteException {
        assert coordinator != null : "Null Coordinator";

        LOG.entering(null, null);
        LOG.fine(String.format("Tester %s leaving with verdict %s", t, v));
        coordinator.quit(t, v);
    }

    /**
     * @see fr.inria.peerunit.Tester#kill()
     */
    public void kill() throws RemoteException {
        for (Tester each : testers) {
            each.kill();
        }
    }

    /**
     * @see fr.inria.peerunit.Tester#setCoordinator(fr.inria.peerunit.Coordinator)
     */
    public void setCoordinator(Coordinator coord) {
        LOG.entering("DistributedTesterImpl", "setCoordinator(Coordinator)");

        this.parent = coord;
    }

    /**
     * Starts the distributed tester:
     * 		- Creates the local tester and the local coordinator.
     *          - Starts all child testers.
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void start() throws RemoteException {
        LOG.entering("DistributedTester", "start()");

        (new Thread(new DistributedTesterThread())).start();

        LOG.exiting("DistributedTester", "start()");


    }

    private void createLocalTester() {
        this.tester = new TesterImpl(this.globalTable(), this.getId(), defaults);
        tester.registerTestCase(testCaseClass);
        this.testers.add(tester);
    }

    private void registerWithParent() throws RemoteException {
        assert parent != null : "Trying to register with a null parent";

        // NB: KeySets are not serializable.
        List<MethodDescription> methods = new ArrayList<MethodDescription>(coordinator.getTesterMap().keySet());
        parent.registerMethods(this, methods);
    }

    private void cleanUp() {
        LOG.fine(String.format("DistributedTester %d cleaning up.", id));
        try {
            testers.clear();
            tester.cleanUp();
            tester = null;
            coordinator.cleanUp();
            coordinator = null;
            bootstrapper = null;
            globals = null;
            parent = null;
            UnicastRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    class DistributedTesterThread implements Runnable {

        public void run() {

            LOG.fine(String.format("Starting Tester %d", id));

            createLocalTester();
            createLocalCoordinator();

            try {
                for (Tester each : testers) {
                    each.setCoordinator(DistributedTesterImpl.this);
                }

                for (Tester each : testers) {
                    each.start();
                }

                Thread tt = new Thread(tester, "LocalTester for DT: " + id);
                tt.start();

                coordinator.waitForTesterRegistration();

                LOG.fine("Registration finished");

                if (parent == null) {
                    LOG.fine(String.format("DistributedTester %d is root", id));
                    Chronometer chrono = new Chronometer();
                    LOG.fine("ROOT: will start the execution.");
                    coordinator.testcaseExecution(chrono);
                    LOG.fine("ROOT: execution finished, waiting for testers to quit.");
                    coordinator.waitAllTestersToQuit();
                    LOG.fine("ROOT: all testers quit, calculating verdict.");
                    coordinator.calculateVerdict(chrono);

                } else {
                    LOG.fine(String.format("DistributedTester %d will register with parent", id));
                    registerWithParent();
                    coordinator.waitAllTestersToQuit();
                    LOG.fine(String.format("DistributedTester %d will now quit", id));
                    parent.quit(DistributedTesterImpl.this, Verdicts.PASS);
                }

                LOG.fine("Waiting for tester thread");
                tt.join();
                LOG.fine("Tester thread finished");
                cleanUp();

                System.exit(0);
                
            } catch (RemoteException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }


        }
    }
}
