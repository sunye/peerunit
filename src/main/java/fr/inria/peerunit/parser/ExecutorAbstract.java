/**
 * ExecutorAbstract
 */
package fr.inria.peerunit.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fr.inria.peerunit.Executor;
import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.PeerUnitLogger;

public abstract class ExecutorAbstract implements Executor {

	protected Map<MethodDescription, Method> methods = new TreeMap<MethodDescription, Method>();
	protected TesterImpl tester;
	protected TestCase testcase;
	protected PeerUnitLogger LOG;
	private int testerId;
	
	/**
	 * Creates an ExecutorImpl
	 * @param t : TesterImpl
	 * @param l : PeerUnitLogger
	 */
	public ExecutorAbstract(TesterImpl t, PeerUnitLogger l) {
		this.tester = t;
		this.LOG=l;
	}
	
	/**
	 * Constructor by default
	 */
	public ExecutorAbstract(){
		
	}

	/**
	 * Verify a peer range (annotation)
	 * 
	 * @param from : number of the first peer
	 * @param to : number of the last peer
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
	protected boolean shouldIExecute(int place, int from, int to) {;
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
	
		return new ArrayList<MethodDescription>(methods.keySet());
	}
	
	/** Get the method following methodDescription
	 * @param md Methode Description
	 * @return the method
	 */
	public Method getMethod(MethodDescription md){
		return methods.get(md);
	}

	/**
	 * Execute the given method description
	 * 
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
	 * @param a Test
	 * @return
	 */
	protected boolean isValid(Test a) {
		return (a != null) && shouldIExecute(a.place(), a.from(), a.to());
	}

	/** 
	 * Test if a BeforeClass is valid and the ExecutorImpl must execute it.	
	 * @param a BeforeClass
	 * @return
	 */
	protected boolean isValid(BeforeClass a) {
		return (a != null) && shouldIExecute(a.place(), a.from(), a.to());
	}

	/** Test if a AfterClass is valid and the ExecutorImpl must execute it.
	 * @param a AfterClass
	 * @return
	 */
	protected boolean isValid(AfterClass a) {
		return (a != null) && shouldIExecute(a.place(), a.from(), a.to());
	}

	/**
	 * @param testerId the testerId to set
	 */
	public void setTesterId(int testerId)
	{
		this.testerId = testerId;
	}

	/**
	 * @return the testerId
	 */
	public int getTesterId()
	{
		return testerId;
	}
	
}