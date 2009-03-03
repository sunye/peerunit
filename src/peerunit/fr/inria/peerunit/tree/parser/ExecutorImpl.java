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
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.Test;
import fr.inria.peerunit.tree.TreeTesterImpl;
import fr.inria.peerunit.util.PeerUnitLogger;

/**
 * This class executes methods of the test case.
 * @author Eduardo
 *
 */
public class ExecutorImpl implements Executor {

	private Map<MethodDescription, Method> methods = new TreeMap<MethodDescription, Method>();
	private TreeTesterImpl tester;
	private TestCaseImpl testcase;
	private PeerUnitLogger LOG;
	
	/**
	 * Creates an ExecutorImpl
	 * @param t : TesterImpl
	 * @param l : PeerUnitLogger
	 */
	public ExecutorImpl(TreeTesterImpl t, PeerUnitLogger l) {
		this.tester = t;
		this.LOG=l;
	}


	/**
	 * Verify a peer range (annotation)
	 * 
	 * @param from : number of the first tester
	 * @param to : number of the last tester
	 * @return boolean : 
	 */
	public boolean validatePeerRange(int from, int to) {
		if ((from > -1) && (to == -1)) {
			throw new AnnotationFailure("Annotation FROM without TO");
		} else if ((from == -1) && (to > -1)) {
			throw new AnnotationFailure("Annotation TO without FROM");
		} else if ((from < -1) || (to < -1)) {
			throw new AnnotationFailure("Invalid value for FROM / TO");
		} else if ((from >= to) && (from != -1)) {
			throw new AnnotationFailure("The value of FROM must be smaller than TO");
		} else return false;
	}

	/** Verify if the tester must execute the method
	 * @param place
	 * @param from : number of the first tester
	 * @param to : number of the last tester
	 * @return
	 */
	private boolean shouldIExecute(int place, int from, int to) {
		int testerId=0;
		try {
			testerId = tester.getId();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return (place == testerId) ||
			(place == -1 && from == -1 && to == -1) ||
			((from <= testerId) && (to >= testerId));
	}

	/**
	 * Parse the test case to extract the methods to be executed
	 * @param class
	 * @return List of methods to be executed
	 */
	public List<MethodDescription> register(Class<? extends TestCase> c) {
		Test t;
		BeforeClass bc;
		AfterClass ac;

		try {
			testcase = (TestCaseImpl) c.newInstance();
			testcase.setTester(tester);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		methods.clear();
		for(Method each : c.getMethods()) {
			t = each.getAnnotation(Test.class);
			if (this.isValid(t)) {
				methods.put(new MethodDescription(each, t), each);				
			}
		}

		for(Method each : c.getMethods()) {
			bc = each.getAnnotation(BeforeClass.class);
			if (this.isValid(bc)) {
				methods.put(new MethodDescription(each, bc), each);				
			}
		}

		for(Method each : c.getMethods()) {
			ac = each.getAnnotation(AfterClass.class);
			if (this.isValid(ac)) {
				methods.put(new MethodDescription(each, ac), each);				
			}
		}
		LOG.log(Level.FINEST,"I will execute the method: "+methods.keySet().toString());
		return new ArrayList<MethodDescription>(methods.keySet());
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

	/**
	 * Verifies if the method is the last one to be executed by its annotation
	 * @param method
	 * @return true if the method is the last one to be executed
	 */
	public boolean isLastMethod(String methodAnnotation) {
		return methodAnnotation.equalsIgnoreCase("AfterClass");
	}

	
	/** 
	 * Test if a Test is valid and the ExecutorImpl must execute it.
	 * @param a
	 * @return
	 */
	private boolean isValid(Test a) {
		return (a != null) && this.shouldIExecute(a.place(), a.from(), a.to());
	}

	/** 
	 * Test if a BeforeClass is valid and the ExecutorImpl must execute it.	
	 * @param a
	 * @return
	 */
	private boolean isValid(BeforeClass a) {
		return (a != null) && this.shouldIExecute(a.place(), a.from(), a.to());
	}

	/** Test if a AfterClass is valid and the ExecutorImpl must execute it.
	 * @param a
	 * @return
	 */
	private boolean isValid(AfterClass a) {
		return (a != null) && this.shouldIExecute(a.place(), a.from(), a.to());
	}

}

