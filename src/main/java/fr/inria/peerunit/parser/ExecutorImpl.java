package fr.inria.peerunit.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;

import fr.inria.peerunit.Executor;
import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.PeerUnitLogger;

/**
 * This class executes methods of the test case.
 * @author Eduardo Almeida
 * @author Jeremy Masson
 *
 */
public class ExecutorImpl extends ExecutorAbstract implements Executor {

	
	/**
	 * Creates an ExecutorImpl
	 * @param t : TesterImpl
	 * @param l : PeerUnitLogger
	 */
	public ExecutorImpl(TesterImpl t, PeerUnitLogger l) {
		super(t, l);
	}
	
	/** Verify if the tester must execute the method
	 * @param place
	 * @param from : number of the first tester
	 * @param to : number of the last tester
	 * @return
	 */
	protected boolean shouldIExecute(int place, int from, int to) {
		setTesterId(tester.getId());
		
		return super.shouldIExecute(place, from, to);
	}
	
	/**
	 * Parse the test case to extract the methods to be executed
	 * @param class
	 * @return List of methods to be executed
	 */
	public List<MethodDescription> register(Class<? extends TestCase> c) {

		try {
			testcase = c.newInstance();
			testcase.setTester(tester);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		List<MethodDescription> listMethod = super.register(c);
	
		LOG.log(Level.FINEST,"I will execute the method: "+methods.keySet().toString());
		return listMethod;
	}

}

