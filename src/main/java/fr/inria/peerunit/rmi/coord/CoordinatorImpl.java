package fr.inria.peerunit.rmi.coord;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.ArchitectureImpl;
import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.GlobalVerdict;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author sunye
 *
 */
public class CoordinatorImpl extends ArchitectureImpl implements Coordinator,
		Runnable, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static int STARTING = 0;
	private static int IDLE = 1;
	private static int RUNNING = 2;
	private static int LEAVING = 3;
	private int status = STARTING;
	

	private Map<MethodDescription, Set<Tester>> testerMap = Collections
			.synchronizedMap(new TreeMap<MethodDescription, Set<Tester>>());

	private List<Tester> registeredTesters;

	/**
	 * Number of expected testers.
	 */
	final private AtomicInteger expectedTesters;


	/**
	 * Number of testers running the current method.
	 */
	private AtomicInteger runningTesters;

	private static final Logger log = Logger.getLogger(CoordinatorImpl.class
			.getName());

	/**
	 * Global verdict, calculated once the test case is executed.
	 */
	private GlobalVerdict verdict;

	/**
	 * Pool of threads. Used to dispatch actions to testers.
	 */
	private ExecutorService executor = Executors.newFixedThreadPool(10);


	/**
	 * @param i Number of expected testers. The Coordinator will wait for
	 * the connection of "i" testers before starting to dispatch actions
	 * to Testers.
	 */
	public CoordinatorImpl(int i) {
		expectedTesters = new AtomicInteger(i);
		runningTesters  = new AtomicInteger(0);
		registeredTesters =  Collections.synchronizedList(new ArrayList<Tester>(i));
		verdict = new GlobalVerdict(TesterUtil.getRelaxIndex());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			// Log creation
			FileHandler handler = new FileHandler(TesterUtil.getLogfile());
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);
			log.setLevel(Level.parse(TesterUtil.getLogLevel()));

			CoordinatorImpl cii = new CoordinatorImpl(TesterUtil
					.getExpectedPeers());
			Coordinator stub = (Coordinator) UnicastRemoteObject.exportObject(
					cii, 0);
			String servAddr = "";
			if (TesterUtil.getServerAddr() == null)
				servAddr = InetAddress.getLocalHost().getHostAddress();
			else
				servAddr = TesterUtil.getServerAddr();

			log.log(Level.INFO, "New Coordinator address is : " + servAddr);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.createRegistry(1099);

			// registry.rebind("Coordinator", stub);
			registry.bind("Coordinator", stub);

			Thread coordination = new Thread(cii, "Coordinator");
			coordination.start();
		} catch (RemoteException e) {
			log.log(Level.SEVERE, "RemoteException", e);
			e.printStackTrace();
		} catch (UnknownHostException e) {
			log.log(Level.SEVERE, "UnknownHostException", e);
			e.printStackTrace();
		} catch (SecurityException e) {
			log.log(Level.SEVERE, "SecurityException", e);
			e.printStackTrace();
		} catch (IOException e) {
			log.log(Level.SEVERE, "IOException", e);
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			log.log(Level.SEVERE, "AlreadyBoundException", e);
			e.printStackTrace();
		}
	}

	/**
	 * @see fr.inria.peerunit.Coordinator#register(fr.inria.peerunit.Tester,
	 *      java.util.List)
	 */
	public synchronized void register(Tester t, List<MethodDescription> list)
			throws RemoteException {
		
		assert status == STARTING : "Trying to regiser while not starting";
		
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
		} catch (RemoteException re) {
			log.warning(re.getMessage());
		} catch (InterruptedException ie) {
			log.warning(ie.getMessage());
		}
	}
	

	/**
	 * Waits for all testers to quit and calculates the global verdict
	 * for a test case.
	 * @param chrono
	 * @throws InterruptedException
	 */
	private synchronized void calculateVerdict(Chronometer chrono)
			throws InterruptedException {

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
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	private void testcaseExecution(Chronometer chrono) throws RemoteException,
			InterruptedException {
		
		assert (status == IDLE) : "Trying to execute test case while not idle";
		
		Set<Tester> testers;
		for (MethodDescription each : testerMap.keySet()) {
			testers = testerMap.get(each);
			log.finest("Method " + each.getName() + " will be executed by " + testers.size() + " testers");
			
			chrono.start(each.getName());
			dispatchMethodToTesters(testers, each);
			chrono.stop(each.getName());
			log.finest("Method "+each + " executed in "+ chrono.getTime(each.getName()) + " msec");
		}
		
		assert (status = LEAVING) == LEAVING;
	}

	/**
	 * Dispatches a given action to a given set of testers.
	 * @param testers
	 * @param md
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	private  void dispatchMethodToTesters(Set<Tester> testers,
			MethodDescription md) throws RemoteException, InterruptedException {
		
		assert (status = RUNNING) == RUNNING;
		
		
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
	public synchronized int getNewId(Tester t) throws RemoteException {
		int id = runningTesters.getAndIncrement();
		log.info("New Registered Peer: " + id + " new client " + t);
		return id;
	}

	public synchronized void methodExecutionFinished() throws RemoteException {
		assert status == RUNNING : "Trying to finish before execution";
		
		
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
	 * Waits for all expected testers to register.
	 */
	private void waitForTesterRegistration() throws InterruptedException {
		assert status == STARTING : "Trying to register while not starting";
		
		log.info("Waiting for registration.");
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

	public String toString() {
		return String.format("Coordinator(expected:%s,registered:%s,running:%s)", this.expectedTesters,this.registeredTesters.size(), this.runningTesters);
	}
}
