package fr.inria.peerunit.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import fr.inria.peerunit.Executor;
import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.PeerUnitLogger;

public class ExecutorImpl implements Executor {

	private Map<MethodDescription, Method> methods = new TreeMap<MethodDescription, Method>();
	private TesterImpl tester;
	private TestCase testcase;
	private PeerUnitLogger LOG;
	
	/*
	 * 
	 * @param t
	 * @param l
	 */
	public ExecutorImpl(TesterImpl t, PeerUnitLogger l) {
		this.tester = t;
		this.LOG=l;
	}

	/*
	 * 
	 * @param from
	 * @param to
	 * @return boolean
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

	private boolean shouldIExecute(int place, int from, int to) {
		int testerId = tester.getId();
		return (place == testerId) ||
			(place == -1 && from == -1 && to == -1) ||
			((from <= testerId) && (to >= testerId));
	}

	/*
	 * 
	 * @param c
	 * @return List<MethodDescription>
	 */
	public List<MethodDescription> register(Class<? extends TestCase> c) {
		Test t;
		BeforeClass bc;
		AfterClass ac;

		try {
			testcase = c.newInstance();
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

	/*
	 * 
	 * @param md
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

	/*
	 * 
	 * @param methodAnnotation
	 * @return boolean
	 */
	public boolean isLastMethod(String methodAnnotation) {
		return methodAnnotation.equalsIgnoreCase("AfterClass");
	}

	private boolean isValid(Test a) {
		return (a != null) && this.shouldIExecute(a.place(), a.from(), a.to());
	}

	private boolean isValid(BeforeClass a) {
		return (a != null) && this.shouldIExecute(a.place(), a.from(), a.to());
	}

	private boolean isValid(AfterClass a) {
		return (a != null) && this.shouldIExecute(a.place(), a.from(), a.to());
	}

}

