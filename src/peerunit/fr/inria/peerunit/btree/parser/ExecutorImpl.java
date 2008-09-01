package fr.inria.peerunit.btree.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import fr.inria.peerunit.Executor;
import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.btree.TreeTesterImpl;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.Test;
import fr.inria.peerunit.util.PeerUnitLogger;

public class ExecutorImpl implements Executor {

	private Map<MethodDescription, Method> methods = new TreeMap<MethodDescription, Method>();
	private TreeTesterImpl tester;
	private TestCaseImpl testcase;
	private PeerUnitLogger LOG;
	private Class<? extends TestCase> c;
	public ExecutorImpl(PeerUnitLogger l) {		
		this.LOG=l;
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
		int testerId=0;		
		testerId = tester.getID();		
		return (place == testerId) ||
			(place == -1 && from == -1 && to == -1) ||
			((from <= testerId) && (to >= testerId));
	}

	public void newInstance(TreeTesterImpl tester){
		this.tester=tester;
		try {
			testcase = (TestCaseImpl) c.newInstance();
			testcase.setTester(this.tester);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}		
	}

	public List<MethodDescription> register(Class<? extends TestCase> c) {
		Test t;
		BeforeClass bc;
		AfterClass ac;		
		this.c=c;
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


	public void invoke(MethodDescription md) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		assert methods.containsKey(md) : "Method should be registered";
		assert testcase != null : "Test Case instance should not be null";

		Method m = methods.get(md);
		m.invoke(testcase, (Object[]) null);
	}

	public boolean isLastMethod(String methodAnnotation) {
		return methodAnnotation.equalsIgnoreCase("AfterClass");
	}

	private boolean isValid(Test a) {
		if(tester!=null)
			return (a != null) && this.shouldIExecute(a.place(), a.from(), a.to());
		else 
			return (a != null) ;
	}

	private boolean isValid(BeforeClass a) {
		if(tester!=null)
			return (a != null) && this.shouldIExecute(a.place(), a.from(), a.to());
		else 
			return (a != null) ;
	}

	private boolean isValid(AfterClass a) {
		if(tester!=null)
			return (a != null) && this.shouldIExecute(a.place(), a.from(), a.to());
		else 
			return (a != null) ;
	}
}

