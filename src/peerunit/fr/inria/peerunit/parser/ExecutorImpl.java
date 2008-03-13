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

public class ExecutorImpl implements Executor {

	private Map<MethodDescription, Method> methods = new TreeMap<MethodDescription, Method>();
	private TesterImpl tester;
	private TestCase testcase;

	public ExecutorImpl(TesterImpl t) {
		this.tester = t;
	}


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


	public List<MethodDescription> register(Class<? extends TestCase> c) {
		Test t;
		BeforeClass bc;
		AfterClass ac;

		try {
			testcase = c.newInstance();
			testcase.setTester(tester);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
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

		return new ArrayList<MethodDescription>(methods.keySet());
	}


	public void invoke(MethodDescription md) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		assert methods.containsKey(md) : "Method should be registered";
		assert testcase != null : "Test Case instance should not be null";

		Method m = methods.get(md);
		m.invoke(testcase, (Object[]) null);
	}

	public boolean isLastMethod(String methodAnnotation) {
		if(methodAnnotation.equalsIgnoreCase("AfterClass"))
			return true;
		else
			return false;
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

