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
import fr.inria.peerunit.parser.ExecutorAbstract;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.Test;

public class ExecutorImpl extends ExecutorAbstract implements Executor {

	private TreeTesterImpl tester;
	private Class<? extends TestCase> c;

	/**
	 * Creates an ExecutorImpl
	 * @param tester : TesterImpl
	 */
	public ExecutorImpl(TreeTesterImpl tester){
		super();
		this.tester = tester;
		setTesterId(tester.getID());
	}
	
	/**
	 * Constructor by default
	 */
	public ExecutorImpl(){
		super();
	}
	
	/**
	 * Parse the test case to extract the methods to be executed
	 * @param class
	 * @return List of methods to be executed
	 */
	public List<MethodDescription> register(Class<? extends TestCase> c) {	
		this.c=c;
		
		return super.register(c);
	}

	/** 
	 * Test if a Test is valid and the ExecutorImpl must execute it.
	 * @param a Test
	 * @return
	 */
	protected boolean isValid(Test a) {
		if(tester!=null)
			return (a != null) && shouldIExecute(a.place(), a.from(), a.to());
		else 
			return (a != null) ;
	}

	/** 
	 * Test if a BeforeClass is valid and the ExecutorImpl must execute it.	
	 * @param a BeforeClass
	 * @return
	 */
	protected boolean isValid(BeforeClass a) {
		if(tester!=null)
			return (a != null) && shouldIExecute(a.place(), a.from(), a.to());
		else 
			return (a != null) ;
	}

	/** Test if a AfterClass is valid and the ExecutorImpl must execute it.
	 * @param a AfterClass
	 * @return
	 */
	protected boolean isValid(AfterClass a) {
		if(tester!=null)
			return (a != null) && shouldIExecute(a.place(), a.from(), a.to());
		else 
			return (a != null) ;
	}
}

