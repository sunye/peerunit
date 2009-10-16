/*
    This file is part of PeerUnit.

    PeerUnit is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PeerUnit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.parser;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author sunye
 * 
 */
public class MethodDescription implements Comparable<MethodDescription>,
		Serializable {
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
	 * Annotation associated to method ("TestStep", "BeforeClass" or "AfterClass")
	 */
	private String annotation;

	/**
	 * Method execution timeout (in milliseconds).
	 */
	private int timeout;

	/*
	 * Create a method description
	 */
	public MethodDescription() {
	}

	/*
	 * Create a method description
	 * 
	 * @param m
	 * @param t
	 */
	public MethodDescription(Method m, TestStep t) {
		this(m.getName(), t.name(), t.step(), t.annotationType()
				.getSimpleName(), t.timeout());
	}

	/*
	 * Create a method description
	 * 
	 * @param m
	 * @param t
	 */
	public MethodDescription(Method m, BeforeClass t) {
		this(m.getName(), "BeforeClass", Integer.MIN_VALUE, t.annotationType()
				.getSimpleName(), t.timeout());
	}

	/*
	 * Create a method description
	 * 
	 * @param m
	 * @param t
	 */
	public MethodDescription(Method m, AfterClass t) {
		this(m.getName(), "AfterClass", Integer.MAX_VALUE, t.annotationType()
				.getSimpleName(), t.timeout());
	}

	/*
	 * Create a method description
	 * 
	 * @param name - the name associated to method
	 * @param testCase - the test case of the method
	 * @param step - the step of the method
	 * @param annotation - the annotation associated to method
	 * @param timeout - the method execution timeout (in milliseconds)
	 */
	public MethodDescription(String name, String testCase, int step,
			String annotation, int timeout) {
		this.name = name;
		this.testCase = testCase;
		this.step = step;
		this.annotation = annotation;
		this.timeout = timeout;
	}

	/*
	 * Change the description of the method
	 * 
	 * @param name - the name associated to method
	 * @param testCase - the test case of the method
	 * @param step - the step of the method
	 * @param annotation - the annotation associated to method
	 * @param timeout - the method execution timeout (in milliseconds)
	 */
	public void setDescription(String name, String testCase, int step,
			String annotation, int timeout) {
		this.name = name;
		this.testCase = testCase;
		this.step = step;
		this.annotation = annotation;
		this.timeout = timeout;
	}

	/*
	 * Compares the step of this object with the step of the specified object for order.
	 * 
	 * @param o - the MethodDescription to be compared. 
	 * @return int 
	 *  -1 if the step of this object is less than the step of the specified object
	 *  0 if the step of this object is equal to the step of the specified object
	 *  1 if the step of this object is greater to the step of the specified object 
	 */
	public int compareTo(MethodDescription o) {

		if (this.testCase.compareTo(o.testCase) == 0) {
			if (this.step > o.step)
				return 1;
			if (this.step < o.step)
				return -1;
			if (this.step == o.step)
				return this.name.compareTo(o.name);

		}
		if (annotation.equals("BeforeClass")) {
			return -1;
		} else if (annotation.equals("AfterClass")) {
			return 1;
		} else if (o.annotation.equals("AfterClass")) {
			return -1;
		} else if (o.annotation.equals("BeforeClass")) {
			return 1;
		} else
			return testCase.compareTo(o.testCase);

	}

	/*
	 * Returns a string representation of the method description . The toString
	 * method returns a string consisting of the name, the test case and the
	 * step of the class of which the object is an instance
	 * 
	 * @return String a string representation of the method description
	 */
	@Override
	public String toString() {
		return String.format("Method: %s TestCase: %s Hierarchy: %d", name,
				testCase, step);
	}

	/*
	 * Indicates whether some other object is "equal to" this one.
	 *  
	 * @param o - the reference object with which to compare. 
	 * @return boolean - true if this object is the same as the object argument; false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MethodDescription)) {
			return false;
		} else {
			MethodDescription other = (MethodDescription) o;
			return name.equals(other.name)
					&& annotation.equals(other.annotation)
					&& step == other.step && testCase.equals(other.testCase)
					&& timeout == other.timeout;
		}
	}

	/*
	 * Returns the name associated to method
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/*
	 * Returns the method execution timeout (in milliseconds)
	 * @return int
	 */
	public int getTimeout() {
		return timeout;
	}

	/*
	 * Returns the annotation associated to method
	 * @return String
	 */
	public String getAnnotation() {
		return annotation;
	}
}
