package fr.inria.peerunit.rmi.coord;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.MessageType;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.Bootstrapper;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.GlobalVerdict;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author sunye
 *
 */
public class CoordinatorImpl implements Coordinator, Bootstrapper,
		Runnable, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final int STARTING = 0;
	private static final int IDLE = 1;
	private static final int RUNNING = 2;
	private static final int LEAVING = 3;
	private int status = STARTING;
	

	private Map<MethodDescription, Set<Tester>> testerMap = Collections
			.synchronizedMap(new TreeMap<MethodDescription, Set<Tester>>());

	final private List<Tester> registeredTesters;

	/**
	 * Number of expected testers.
	 */
	final private AtomicInteger expectedTesters;


	/**
	 * Number of testers running the current method (test step).
	 */
	final private AtomicInteger runningTesters;

	private static final Logger log = Logger.getLogger(CoordinatorImpl.class
			.getName());

	/**
	 * Global verdict, calculated once the test case is executed.
	 */
	private GlobalVerdict verdict;

	/**
	 * Pool of threads. Used to dispatch actions to testers.
	 */
	private ExecutorService executor;

	
	/**
	 * @param i Number of expected testers. The Coordinator will wait for
	 * the connection of "i" testers before starting to dispatch actions
	 * to Testers.
	 */
	public CoordinatorImpl(int testerNbr, int relaxIndex) {
		expectedTesters = new AtomicInteger(testerNbr);
		runningTesters  = new AtomicInteger(0);
		registeredTesters =  Collections.synchronizedList(new ArrayList<Tester>(testerNbr));
		verdict = new GlobalVerdict(relaxIndex);
		executor = Executors.newFixedThreadPool(testerNbr > 10 ? 10 : testerNbr);
	}
	
	
	public CoordinatorImpl(TesterUtil tu)  {
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
		
		if (registeredTesters.size() >= expectedTesters.intValue()) {
			log.warning("More registrations than expected");
			return;
		}
		
		for (MethodDescription m : list) {
			if (!testerMap.containsKey(m)) {
				testerMap.put(m, Collections.synchronizedSet(new HashSet<Tester>()));
			}
			testerMap.get(m).add(t);
		}
		registeredTesters.add(t);
		synchronized (registeredTesters) {
			registeredTesters.notifyAll();
		}
	}

	public void run() {
		Chronometer chrono = new Chronometer();
		try {
			waitForTesterRegistration();
			testcaseExecution(chrono);
			waitAllTestersToQuit();
			calculateVerdict(chrono);
			cleanUp();
			
		} catch (InterruptedException ie) {
			log.warning(ie.getMessage());
		}
	}
	

	/**
	 * Waits for all testers to quit and calculates the global verdict
	 * for a test case.
	 * @param chrono
	 */
	private  void calculateVerdict(Chronometer chrono) {

		for (Map.Entry<String, ExecutionTime> entry : chrono.getExecutionTime()) {
			log.log(Level.INFO, "Method " + entry.getKey() + " executed in "
					+ entry.getValue());
		}
		log.info("Test Verdict: " + verdict);

	}

	/**
	 * Dispatches actions to testers:
	 * 
	 * @param chrono
	 * @throws InterruptedException
	 */
	private void testcaseExecution(Chronometer chrono) throws InterruptedException {
		
		assert (status == IDLE) : "Trying to execute test case while not idle";
		
		
		for (MethodDescription each : testerMap.keySet()) {
			
			chrono.start(each.getName());
			execute(each);
			chrono.stop(each.getName());
			log.finest("Method "+each + " executed in "+ chrono.getTime(each.getName()) + " msec");
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
	public  void execute(MethodDescription md) throws InterruptedException {
		
		assert (status = RUNNING) == RUNNING;
		
		Set<Tester> testers = testerMap.get(md);
		log.finest("Method " + md + " will be executed by " + testers.size() + " testers");

		runningTesters.set(testers.size());
		for (Tester each : testers) {
			log.finest("Dispatching "+md+" to tester "+each);
			executor.submit(new MethodExecute(each, md));
		}
		waitForExecutionFinished();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see callback.Coordinator#namer(callback.Tester) Incremented with
	 * java.util.concurrent to handle the semaphore concurrency access
	 */
	public synchronized int register(Tester t) throws RemoteException {
		int id = runningTesters.getAndIncrement();
		log.info("New Registered Tester: " + id + " new client " + t);
		return id;
	}

	public synchronized void methodExecutionFinished(Tester tester, MessageType message) throws RemoteException {
		assert status == RUNNING : "Trying to finish before execution";
		
		
		// TODO Improve the usage of the results.
		runningTesters.decrementAndGet();
		synchronized (runningTesters) {
			runningTesters.notifyAll();
		}
	}

	public synchronized void quit(Tester t, Verdicts localVerdict) throws RemoteException {
		assert status == LEAVING : "Trying to quit during execution";
		
		verdict.addLocalVerdict(localVerdict);
		synchronized (registeredTesters) {
			registeredTesters.remove(t);
			registeredTesters.notifyAll();
		}
	}

	/**
	 * @return A read-only map of (Methods X Testers).
	 */
	public Map<MethodDescription, Set<Tester>> getTesterMap() {
		return Collections.unmodifiableMap(this.testerMap);
	}

	/**
	 * Waits for all expected testers to registerMethods.
	 */
	public void waitForTesterRegistration() throws InterruptedException {
		assert status == STARTING : "Trying to register while not starting";
		
		log.info("Waiting for registration. Expecting "+expectedTesters+" testers.");
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
		
		log.info("Waiting for the end of the execution.");
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
	private void waitAllTestersToQuit() throws InterruptedException {
		assert status == LEAVING : "Trying to quit before time";
		
		log.info("Waiting all testers to quit.");
		while (registeredTesters.size() > 0) {
			log.info(String.valueOf(registeredTesters.size()));
			synchronized (registeredTesters) {
				registeredTesters.wait();
			}
		}
		log.info("All testers quit.");
	}

	/**
	 * Clears references to testers.
	 */
	private void cleanUp() {
		log.info("Cleaning");
		testerMap.clear();
		runningTesters.set(0);
		registeredTesters.clear();
		executor.shutdown();
	}

	@Override
	public String toString() {
		return String.format("Coordinator(expected:%s,registered:%s,running:%s)", this.expectedTesters, new Integer(this.registeredTesters.size()), this.runningTesters);
	}


    public boolean isRoot(int id) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
