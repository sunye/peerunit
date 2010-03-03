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
package fr.inria.peerunit.tester;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.base.AbstractTester;
import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.base.SingleResult;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.remote.Tester;
import fr.inria.peerunit.util.TesterUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.ClosedByInterruptException;

/**
 * @author Eduardo Almeida
 * @author Jeremy Masson
 * @author sunye
 * @version 1.0
 * @since 1.0
 * @see fr.inria.peerunit.remote.peerUnit.Tester
 * @see fr.inria.peerunit.VolatileTester
 * @see fr.inria.peerunit.remote.StorageTester
 * @see fr.inria.peerunit.remote.Coordinator
 * @see java.util.concurrent.BlockingQueue<Object>
 */
public class TesterImpl extends AbstractTester implements Tester, Serializable, Runnable {

    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(TesterImpl.class.getName());
    private transient Coordinator coord;
    private transient Bootstrapper bootstrapper;
    private transient boolean stop = false;
    private transient TestCaseWrapper testCase;
    private transient BlockingQueue<MethodDescription> executionQueue = new ArrayBlockingQueue<MethodDescription>(2);
    
    private Class<?> testCaseClass;

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
        testCase = new TestCaseWrapper(this);
        this.initializeLogger();
    }

    public TesterImpl(Bootstrapper boot, GlobalVariables gv, TesterUtil tu) throws RemoteException {
        this(boot, gv);
        defaults = tu;
    }

    protected TesterImpl(GlobalVariables gv, int i, TesterUtil tu) {
        super(gv);
        defaults = tu;
        this.setId(i);
        testCase = new TestCaseWrapper(this);
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
        Thread invocationThread;

        while (!stop) {
            MethodDescription md = null;
            try {
                md = executionQueue.poll(defaults.getWaitForMethod(), TimeUnit.MILLISECONDS);
                if (md != null) {
                    invocationThread = new Thread(new Invoke(md));

                    if (md.getTimeout() > 0) {
                        timeoutThread = new Thread(new Timeout(invocationThread, md.getTimeout()));
                        timeoutThread.start();
                    }
                    invocationThread.start();
                }
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, "TesterImpl:run() - InterruptedException", e);
            }
        }
        LOG.fine("Stopping Tester ");
        try {
            coord.quit(this);
        } catch (RemoteException e) {
            LOG.log(Level.SEVERE, "Error calling Coordinator.quit()", e);
        } 
    }

    /**
     * Creates the peer and the test testCase.
     * Sends the actions to be executed to the testCase.
     *
     * @param klass the Test Case Class.
     * @throws RemoteException
     * @throws SecurityException
     */
    public void registerTestCase(Class<?> klass) {
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
        LOG.log(Level.FINE, "Starting TesterImpl::execute(MethodDescription) with: " + md);

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
        quit();
        LOG.log(Level.INFO, "Test Case finished by kill ");
    }

    /**
     *  Used to signal the finish of an method execution. If the method is
     *  the last action of the test case, the execution of this test case
     *  is interrupted.
     *  @param methodAnnotation the method which was executed
     */
    private void executionFinished(ResultSet r) {
        assert coord != null : "Null coordinator";

        try {
            coord.methodExecutionFinished(r);
            if (testCase.isLastMethod()) {
                LOG.log(Level.FINEST, "Test Case finished");
                quit();
            }
        } catch (RemoteException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }

        }
    }

    /**
     *  Used to interrupt actions's execution. 
     *  Cleans the action list and asks coordinator to quit.
     */
    public void quit() {
        assert coord != null : "Null coordinator";

        try {
            executionQueue.clear();
            coord.quit(this);
        } catch (RemoteException e) {
            LOG.log(Level.SEVERE, "Remote error during quit().", e);
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

        SingleResult result = new SingleResult(id, md);
        try {
            result.start();
            testCase.invoke(md);
            if (Thread.interrupted()) {
                result.addInconclusive(null);
                LOG.finest("Thread was interrupted.");
            }
        } catch (InconclusiveFailure e) {
            LOG.log(Level.WARNING, "InconclusiveFailure", e);
            result.addInconclusive(e);
        } catch (TestException e) {
            LOG.log(Level.WARNING, "TestException", e);
            result.addFailure(e);
        } catch (AssertionError e) {
            LOG.log(Level.WARNING, "AssertionError", e);
            result.addFailure(e);
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING, "InterruptedException", e);
            result.addInconclusive(e);
        } catch (ClosedByInterruptException e) {
            LOG.log(Level.WARNING, "ClosedByInterruptException", e);
            result.addInconclusive(null);
        } catch (Throwable e) {
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            e.printStackTrace(pw);
            pw.flush();writer.flush();
            LOG.log(Level.WARNING, writer.toString());
            result.addError(e);
        } finally {
            result.stop();
        }

        LOG.log(Level.FINEST, "Tester [" + id + "] Executed " + md);
        this.executionFinished(result.asResultSet());
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
