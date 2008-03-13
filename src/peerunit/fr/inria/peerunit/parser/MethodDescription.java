package fr.inria.peerunit.parser;


import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author sunye
 *
 */
public class MethodDescription implements Comparable<MethodDescription>, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Method name
	 */
	private String name;

	private String testCase;

	private int step;


	/**
	 * Annotation associated to method ("Test", "BeforeClass" or "AfterClass")
	 */
	private String annotation;


	/**
	 * Method execution timeout (in milliseconds).
	 */
	private int timeout;

	public MethodDescription(){}

	public MethodDescription(Method m, Test t) {
		this(m.getName(), t.name(), t.step(), t.annotationType().getSimpleName(), t.timeout());
	}

	public MethodDescription(Method m, BeforeClass t) {
		this(m.getName(), "BeforeClass", Integer.MIN_VALUE, t.annotationType().getSimpleName(), t.timeout());
	}

	public MethodDescription(Method m, AfterClass t) {
		this(m.getName(), "AfterClass", Integer.MAX_VALUE, t.annotationType().getSimpleName(), t.timeout());
	}

	public  MethodDescription(String name, String testCase, int step, String annotation, int timeout){
		this.name = name;
		this.testCase = testCase;
		this.step = step;
		this.annotation = annotation;
		this.timeout = timeout;
	}



	public void setDescription(String name, String testCase, int step, String annotation, int timeout){
		this.name = name;
		this.testCase = testCase;
		this.step = step;
		this.annotation=annotation;
		this.timeout=timeout;
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

	@Override
	public boolean equals(Object o) {
		if (! (o instanceof MethodDescription)) {
			return false;
		} else {
			MethodDescription other = (MethodDescription) o;
			return name.equals(other.name) && annotation.equals(other.annotation) &&
				step == other.step && testCase.equals(other.testCase) && timeout == other.timeout;
		}
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
