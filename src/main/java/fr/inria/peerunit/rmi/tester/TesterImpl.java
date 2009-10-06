package fr.inria.peerunit.rmi.tester;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Bootstrapper;
import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.MessageType;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.base.AbstractTester;
import fr.inria.peerunit.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Oracle;
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
    private boolean stop = false;
    private transient Thread timeoutThread;
    private transient Thread invokationThread;
    private transient ExecutorImpl executor;
    private Verdicts v = Verdicts.PASS;
    private transient BlockingQueue<MethodDescription> executionQueue = new ArrayBlockingQueue<MethodDescription>(2);
    private TesterUtil defaults = TesterUtil.instance;

    /**
     * Used to give the identifier of the tester.
     *
     * @param c the coordinator which give the tester's identifier.
     * @throws RemoteException
     */
    public TesterImpl(Bootstrapper boot, GlobalVariables gv) throws RemoteException {
        super(gv);
        int id = boot.register(this);
        this.setId(id);

    }

    public TesterImpl(Bootstrapper boot, GlobalVariables gv, TesterUtil tu) throws RemoteException {
        this(boot, gv);
        defaults = tu;
    }
    
    protected TesterImpl(GlobalVariables gv, int i, TesterUtil tu) {
    	super(gv);
    	defaults = tu;
    	this.setId(i);
    }

    public void setCoordinator(Coordinator c) {
    	assert c != null : "Null coordinator";
    	
    	this.coord = c;
    }
    
    /**
     * starts the tester
     *
     * @throws InterruptedException
     */
    public void run() {
    	assert coord != null : "Null coordinator";
    	
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
                for (StackTraceElement each : e.getStackTrace()) {
                    LOG.severe(each.toString());
                }

            }
        }
        LOG.log(Level.INFO, "Stopping Tester ");
        try {
            coord.quit(this, v);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Creates the peer and the test executor.
     * Sends the actions to be executed to the executor.
     *
     * @param c the peer to be created.
     * @throws RemoteException
     * @throws SecurityException
     */
    public void export(Class<? extends TestCaseImpl> c) {
    	assert coord != null : "Null coordinator";

        boolean exported = false;
        try {
            
            executor = new ExecutorImpl(this, LOG);
            coord.registerMethods(this, executor.register(c));
            exported = true;
        } catch (RemoteException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }
        } catch (SecurityException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }
        } finally {
            if (!exported) {
                executionInterrupt();
            }
        }
    }

    /**
     * Used to add an action to be executed
     *
     * @throws RemoteException
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
    private void executionOk(MethodDescription md) {
    	assert coord != null : "Null coordinator";
    	
        try {
            coord.methodExecutionFinished(this, MessageType.OK);
            LOG.log(Level.FINEST, "Executed " + md.getName());
            if (executor.isLastMethod(md.getAnnotation())) {
                LOG.log(Level.FINEST, "Test Case finished by annotation " + md.getAnnotation());
                executionInterrupt();
            }
        } catch (RemoteException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }

        }
    }

    /**
     *  Used to interrupt actions's execution. Cleans the action'list and give a local verdict
     */
    public void executionInterrupt() {
    	assert coord != null : "Null coordinator";
    	
        try {
            if (v == null) {
                v = Verdicts.INCONCLUSIVE;
                //error=true;
            }
            executionQueue.clear();
            LOG.log(Level.INFO, "Test Case local verdict to peer " + getId() + " is " + v.toString());
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
        assert executor != null : "Null executor";

        boolean error = true;
        try {
            executor.invoke(md);
            error = false;
        } catch (IllegalArgumentException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }

        } catch (IllegalAccessException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }

        } catch (InvocationTargetException e) {
            Oracle oracle = new Oracle(e.getCause());
            if (oracle.isPeerUnitFailure()) {
                error = false;
            }
            v = oracle.getVerdict();
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }

        } finally {
            if (error) {
                LOG.log(Level.WARNING, " Executed in " + md.getName());
                executionInterrupt();
            } else {
                LOG.log(Level.INFO, " Executed " + md.getName());
                executionOk(md);
            }
        }


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

	public void start() throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
