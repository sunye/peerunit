package fr.inria.peerunit.btree.parser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fr.inria.peerunit.Executor;
import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.btree.TreeTesterImpl;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.Test;

public class ExecutorImpl implements Executor {

	private Map<MethodDescription, Method> methods = new TreeMap<MethodDescription, Method>();
	private TreeTesterImpl tester;
	//private TestCaseImpl testcase;	
	private Class<? extends TestCase> c;
	//private static Logger PEER_LOG;
	int testerId;
		
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
		int testerId = tester.getID();		
		return (place == testerId) ||
			(place == -1 && from == -1 && to == -1) ||
			((from <= testerId) && (to >= testerId));
	}	
	
	public Method getMethod(MethodDescription md){
		return methods.get(md);
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
		return new ArrayList<MethodDescription>(methods.keySet());
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

