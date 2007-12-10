package fr.inria.peerunit.parser;


import java.io.Serializable;

public class MethodDescription implements Comparable<MethodDescription>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;

	private String testCase;

	private int step;
	
	private String annotation;
	
	private int timeout;
	
	public MethodDescription(String n, String tc, int i, String a, int t) {
		name = n;
		testCase = tc;
		step = i;
		annotation=a;
		timeout=t;
	}

	public int compareTo(MethodDescription o) {
		
		if (this.testCase.compareTo(o.testCase) == 0) {
			if (this.step > o.step)
				return 1;
			if (this.step < o.step)
				return -1;
			if (this.step == o.step)
				return this.name.compareTo(o.name);
							
		}
		if(annotation.equals("BeforeClass")){
			return -1;
		}else if(annotation.equals("AfterClass")){
			return 1;
		}else if(o.annotation.equals("AfterClass")){
			return -1;
		}else if(o.annotation.equals("BeforeClass")){
			return 1;
		}else	
			return testCase.compareTo(o.testCase);
				
	}

	@Override
	public String toString() {
		return String.format("Method: %s TestCase: %s Hierarchy: %d", name, testCase, step);
	}
	
	public String getName(){
		return name;
	}
	
	public int getTimeout(){
		return timeout;
	}
	
	public String getAnnotation(){
		return annotation;
	}
}
