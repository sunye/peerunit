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
import java.rmi.RemoteException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Bootstrapper;
import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.base.AbstractTester;
import fr.inria.peerunit.base.Result;
import fr.inria.peerunit.base.TestCaseWrapper;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author Eduardo Almeida
 * @author Jeremy Masson
 * @version 1.0
 * @since 1.0
 * @see fr.inria.peerUnit.Tester
 * @see fr.inria.peerunit.VolatileTester
 * @see fr.inria.peerunit.StorageTester
 * @see fr.inria.peerunit.Coordinator
 * @see java.util.concurrent.BlockingQueue<Object>
 */
public class TesterImpl extends AbstractTester implements Tester, Serializable, Runnable {

    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(TesterImpl.class.getName());
    private transient Coordinator coord;
    private transient Bootstrapper bootstrapper;
    private transient boolean stop = false;

    private transient TestCaseWrapper testCase;
    private Verdicts v = Verdicts.PASS;
    private transient BlockingQueue<MethodDescription> executionQueue = new ArrayBlockingQueue<MethodDescription>(2);
    private transient TesterUtil defaults = TesterUtil.instance;

    private Class<? extends TestCaseImpl> testCaseClass;

    /**
     * Used to give the identifier of the tester.
     *
     * @param klass the coordinator which give the tester's identifier.
     * @throws RemoteException
     */
    public TesterImpl(Bootstrapper boot, GlobalVariables gv) throws RemoteException {
        super(gv);
        bootstrapper = boot;

        this.setId(bootstrapper.register(this));
        testCase = new TestCaseWrapper(this, LOG);
    }

    public TesterImpl(Bootstrapper boot, GlobalVariables gv, TesterUtil tu) throws RemoteException {
        this(boot, gv);
        defaults = tu;
    }
    
    protected TesterImpl(GlobalVariables gv, int i, TesterUtil tu) {
    	super(gv);
    	defaults = tu;
    	this.setId(i);
        testCase = new TestCaseWrapper(this, LOG);
    }

    public void setCoordinator(Coordinator c) {
        LOG.entering("TesterImpl", "setCoordinator");
    	assert c != null : "Null coordinator";
    	this.coord = c;
    }


    public void start() throws RemoteException {
        LOG.entering("TesterImpl", "start()");
    	assert coord != null : "Null coordinator";

        coord.registerMethods(this, testCase.register(testCaseClass));
    }

    /**
     * starts the tester
     *
     * @throws InterruptedException
     */
    public void run() {
        assert coord != null : "Null coordinator";
        LOG.entering("TesterImpl", "run()");

        Thread timeoutThread;
        Thread invokationThread;

        while (!stop) {
            MethodDescription md = null;
            try {
                md = executionQueue.poll(defaults.getWaitForMethod(), TimeUnit.MILLISECONDS);
                if (md != null) {
                    invokationThread = new Thread(new Invoke(md));
                    invokationThread.start();
                    if (md.getTimeout() > 0) {
                        timeoutThread = new Thread(new Timeout(invokationThread,
                                md.getTimeout()));
                        timeoutThread.start();
                    }
                }
            } catch (InterruptedException e) {
                Result r = new Result(id, md);
                r.addTimeout(e);
                this.executionFinished(r);
            }
        }
        LOG.fine("Stopping Tester ");
        try {
            coord.quit(this, v);
        } catch (RemoteException e) {
            LOG.log(Level.SEVERE,"Error calling Coordinator.quit()",e);
        }
        //System.exit(0);
    }

    /**
     * Creates the peer and the test testCase.
     * Sends the actions to be executed to the testCase.
     *
     * @param klass the Test Case Class.
     * @throws RemoteException
     * @throws SecurityException
     */
    public void registerTestCase(Class<? extends TestCaseImpl> klass) {
        LOG.entering("TesterImpl", "registerTestCase(CLass)");
        testCaseClass = klass;
        
    }

    /**
     * Used to add an action to be executed
     *
     * @throws RemoteExcption
     * @throws InterruptedException
     */
    public synchronized void execute(MethodDescription md)
            throws RemoteException {
        LOG.log(Level.FINEST, "Starting TesterImpl::execute(MethodDescription) with: " + md);
        try {
            executionQueue.put(md);
        } catch (InterruptedException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }

        }
    }

    /**
     * An example how to kill a peer
     * <code> YourTestClass test = new YourTestClass();
     * test.export(test.getClass());
     * test.run();
     * ...	 // code
     * test.kill(); </code>
     */
    public void kill() {
        executionInterrupt();
        LOG.log(Level.INFO, "Test Case finished by kill ");
    }

    /**
     *  Used to signal the finish of an method execution. If the method is
     *  the last action of the test case, the execution of this test case
     *  is interrupted.
     *  @param methodAnnotation the method which was executed
     */
    private void executionFinished(Result r) {
    	assert coord != null : "Null coordinator";

        MethodDescription md = r.getMethodDescription();
        try {
            coord.methodExecutionFinished(r);
            
            if (testCase.isLastMethod()) {
                LOG.log(Level.FINEST, "Test Case finished");
                executionInterrupt();
            }
        } catch (RemoteException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }

        }
    }

    /**
     *  Used to interrupt actions's execution. 
     *  Cleans the action list and gives a local verdict
     */
    public void executionInterrupt() {
    	assert coord != null : "Null coordinator";
    	
        try {
            if (v == null) {
                v = Verdicts.INCONCLUSIVE;
                //error=true;
            }
            executionQueue.clear();
            LOG.fine(String.format("Local verdict to tester %d is %s",id, v));
            //coord.quit(this,error,v);
            coord.quit(this, v);
        } catch (RemoteException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }

        } finally {
            stop = true;
        }
    }


    /**
     *  Used to invoke an action
     * @param md the action will be invoked
     */
    private synchronized void invoke(MethodDescription md) {
        assert testCase != null : "Null executor";

        Result result = new Result(id, md);
        try {
            result.start();
            testCase.invoke(md);   
        } catch (AssertionError e) {
            result.addFailure(e);
        } catch (Throwable e) {
            result.addError(e);
        } finally {
            result.stop();
        }
        
        LOG.log(Level.FINEST, "Tester ["+id+"] Executed " + md);
        this.executionFinished(result);
    }


    public void cleanUp() {
        LOG.fine("Tester cleaning up.");
        globals = null;
        bootstrapper = null;
        coord = null;
    }

    /**
     * @author Eduardo Almeida.
     * @version 1.0
     * @since 1.0
     * @see java.lang.Runnable
     */
    private class Invoke implements Runnable {

        MethodDescription md;

        public Invoke(MethodDescription md) {
            this.md = md;
        }

        public void run() {
            invoke(md);
        }
    }

}
