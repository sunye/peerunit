/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.rmi.tester;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.Executor;
import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.MessageType;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.base.AbstractTester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Verdicts;

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
public class DistributedTester extends AbstractTester implements Tester, Coordinator {
    
	private static Logger LOG = Logger.getLogger(TesterImpl.class.getName());
	
	/**
	 * The coordinator of this tester. Since DistributedTester is used in
	 * a distributed architecture, the coordinator is also a DistributedTester.
	 * 
	 */
	private Coordinator coordinator;
	
	/**
	 * Set of testers that are coordinated by this tester.
	 */
	private List<Tester> testers = new LinkedList<Tester>();

	/**
	 * A correspondence table MethodDescription X Set of Testers.
	 */
	private Map<MethodDescription, Set<Tester>> testerMap = Collections
	.synchronizedMap(new TreeMap<MethodDescription, Set<Tester>>());
	
	private transient Executor executor;
	
	private transient Class<? extends TestCaseImpl> testCaseClass;
	
    
    private transient DistributedTesterThread thread;
	
    public DistributedTester(GlobalVariables gv) {
        super(gv);
        thread = new DistributedTesterThread();
    }

    
    /** 
     * @see fr.inria.peerunit.Coordinator#registerMethods(fr.inria.peerunit.Tester, java.util.List)
     */
    public void registerMethods(Tester tester, List<MethodDescription> list) throws RemoteException {
    	assert coordinator != null : "Null Coordinator";
    	
		for (MethodDescription m : list) {
			if (!testerMap.containsKey(m)) {
				testerMap.put(m, Collections.synchronizedSet(new HashSet<Tester>()));
			}
			testerMap.get(m).add(tester);
		}
		testers.add(tester);
		
		coordinator.registerMethods(tester, list);
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
	 * @see fr.inria.peerunit.Tester#execute(fr.inria.peerunit.parser.MethodDescription)
	 */
	public void execute(MethodDescription md) throws RemoteException {
		thread.execute(md);
		for (Tester each : testers) {	
			each.execute(md);
		}
	}

	/** 
	 * @see fr.inria.peerunit.Tester#kill()
	 */
	public void kill() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	
	/** 
	 * @see fr.inria.peerunit.Tester#setCoordinator(fr.inria.peerunit.Coordinator)
	 */
	public void setCoordinator(Coordinator coord) {
		this.coordinator = coord;
		
	}
	
	
	/**
	 * Sets the test case class.
	 * 
	 * @param klass The test case class to execute
	 */
	public void setTestCaseClass(Class<? extends TestCaseImpl> klass) {
		this.testCaseClass = klass;
	}

	
	
	/**
	 * Starts the distributed tester:
	 * 		- Parses the test case class
	 * 		- Registers the test steps with the coordinator
	 * 		- Starts the DistributedTesterThread
	 * @throws RemoteException
	 */
	public void start() throws RemoteException {
		assert coordinator != null;
		assert executor != null;
		assert testCaseClass != null;
		
		List<MethodDescription> methods = executor.register(testCaseClass);
		coordinator.registerMethods(this, methods);

		thread.start();
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

        } catch (IllegalAccessException e) {

        } catch (InvocationTargetException e) {

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
     *  Used to signal the finish of an method execution. If the method is
     *  the last action of the test case, the execution of this test case
     *  is interrupted.
     *  @param methodAnnotation the method which was executed
     */
    private void executionOk(MethodDescription md) {

    }

    /**
     *  Used to interrupt actions's execution. Cleans the action'list and give a local verdict
     */
    private void executionInterrupt() {

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
    
    private class DistributedTesterThread extends Thread {
    	
    	private boolean shouldIStop = false;
    	
    	private BlockingQueue<MethodDescription> executionQueue = new ArrayBlockingQueue<MethodDescription>(2);

    	
        /**
		 * starts the tester
		 * 
		 * @throws InterruptedException
		 */
		public void run() {
			Thread invokationThread;
			Thread timeoutThread;
			int timeout;

			while (!shouldIStop) {
				MethodDescription md = null;
				md = executionQueue.poll();
				invokationThread = new Thread(new Invoke(md));
				invokationThread.start();
				timeout = md.getTimeout();
				if (timeout > 0) {
					timeoutThread = new Thread(new Timeout(invokationThread,timeout));
					timeoutThread.start();
				}
			}

		}
		
		public void execute(MethodDescription md) {
			executionQueue.offer(md);
		}
    }
}
