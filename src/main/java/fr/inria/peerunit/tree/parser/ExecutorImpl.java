package fr.inria.peerunit.tree.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import fr.inria.peerunit.Executor;
import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.ExecutorAbstract;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.Test;
import fr.inria.peerunit.tree.TreeTesterImpl;
import fr.inria.peerunit.util.PeerUnitLogger;

/**
 * This class executes methods of the test case.
 * @author Eduardo
 * @author Jeremy Masson
 */
public class ExecutorImpl extends ExecutorAbstract implements Executor {

	private TreeTesterImpl tester;
	private TestCaseImpl testcase;
	
	/**
	 * Creates an ExecutorImpl
	 * @param t : TreeTesterImpl
	 * @param l : PeerUnitLogger
	 */
	public ExecutorImpl(TreeTesterImpl t, PeerUnitLogger l) {
		super();
		this.tester = t;
		this.LOG=l;
	}


	/** Verify if the tester must execute the method
	 * @param place
	 * @param from : number of the first tester
	 * @param to : number of the last tester
	 * @return
	 */
	protected boolean shouldIExecute(int place, int from, int to) {
		try {
			setTesterId(tester.getId());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return super.shouldIExecute(place, from, to);
	}

	/**
	 * Parse the test case to extract the methods to be executed
	 * @param class
	 * @return List of methods to be executed
	 */
	public List<MethodDescription> register(Class<? extends TestCase> c) {

		try {
			testcase = (TestCaseImpl) c.newInstance();
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


	/** Execute the given method description
	 * @param md : method description to execute
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void invoke(MethodDescription md) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		assert methods.containsKey(md) : "Method should be registered";
		assert testcase != null : "Test Case instance should not be null";

		Method m = methods.get(md);
		m.invoke(testcase, (Object[]) null);
	}

	
	

}

