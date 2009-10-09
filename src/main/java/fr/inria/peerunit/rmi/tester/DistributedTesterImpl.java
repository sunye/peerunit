/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.rmi.tester;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
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
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.rmi.coord.Chronometer;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.TesterUtil;
import java.util.ArrayList;

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

            parent.methodExecutionFinished(this, MessageType.OK);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    /**
     * @see fr.inria.peerunit.Coordinator#methodExecutionFinished(Tester, fr.inria.peerunit.MessageType)
     */
    public void methodExecutionFinished(Tester tester, MessageType message) throws RemoteException {
        assert coordinator != null : "Null Coordinator";

        coordinator.methodExecutionFinished(tester, message);
    }

    /** 
     * @see fr.inria.peerunit.Coordinator#quit(fr.inria.peerunit.Tester, fr.inria.peerunit.test.oracle.Verdicts)
     */
    public void quit(Tester t, Verdicts v) throws RemoteException {
        assert coordinator != null : "Null Coordinator";
        
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
        LOG.fine(String.format("Starting Tester %d", this.getId()));
        
        this.createLocalTester();
        this.createLocalCoordinator();

        for (Tester each : testers) {
            each.setCoordinator(this);
        }

        for (Tester each : testers) {
            each.start();
        }

        Thread tt = new Thread(tester, "LocalTester for DT: " + id);
        tt.start();

        try {
            coordinator.waitForTesterRegistration();
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        LOG.info("Registration finished");

        if (parent == null) {
            LOG.info(String.format("DistributedTester %d is root", id));
            Chronometer chrono = new Chronometer();
            try {
                coordinator.testcaseExecution(chrono);
 //               coordinator.waitAllTestersToQuit();
//                coordinator.calculateVerdict(chrono);
//                coordinator.cleanUp();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } else {
            LOG.info(String.format("DistributedTester %d will register with parent", id));
            this.registerWithParent();
        }
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
}
