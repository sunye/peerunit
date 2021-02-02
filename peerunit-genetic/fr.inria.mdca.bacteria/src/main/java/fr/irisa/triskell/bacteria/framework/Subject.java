/*
 * Created on 29 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package fr.irisa.triskell.bacteria.framework;


import java.util.ArrayList;
import java.util.Collection;



import java.util.Iterator;


/** * @author bbaudry * 29 nov. 2004 */
public class Subject {

	public void addObserver(BacteriologicAlgorithmObserver observer) {
		bacteriologicAlgorithmObserver.add(observer);
	}

	/**
	 *  
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 * @uml.associationEnd multiplicity="(0 -1)" ordering="ordered" elementType="bacteriologicFramework.BacteriologicAlgorithmObserver"
	 */
	private ArrayList bacteriologicAlgorithmObserver;



	public void removeObserver(BacteriologicAlgorithmObserver observer) {
	}

	public void notifySolutionSetChange(BacteriologicAlgorithm algo) {
		Iterator it = bacteriologicAlgorithmObserverIterator();
		while (it.hasNext()){
			((BacteriologicAlgorithmObserver)it.next()).updateSolutionSet(algo);
		}
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public java.util.ArrayList getBacteriologicAlgorithmObserver() {
		return bacteriologicAlgorithmObserver;
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public void setBacteriologicAlgorithmObserver(java.util.ArrayList value) {
		bacteriologicAlgorithmObserver = value;
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public Iterator bacteriologicAlgorithmObserverIterator() {
		return bacteriologicAlgorithmObserver.iterator();
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public boolean addBacteriologicAlgorithmObserver(
		fr.irisa.triskell.bacteria.framework.BacteriologicAlgorithmObserver element) {
		return bacteriologicAlgorithmObserver.add(element);
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public boolean removeBacteriologicAlgorithmObserver(
		fr.irisa.triskell.bacteria.framework.BacteriologicAlgorithmObserver element) {
		return bacteriologicAlgorithmObserver.remove(element);
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public boolean isBacteriologicAlgorithmObserverEmpty() {
		return bacteriologicAlgorithmObserver.isEmpty();
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public void clearBacteriologicAlgorithmObserver() {
		bacteriologicAlgorithmObserver.clear();
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public boolean containsBacteriologicAlgorithmObserver(
		fr.irisa.triskell.bacteria.framework.BacteriologicAlgorithmObserver element) {
		return bacteriologicAlgorithmObserver.contains(element);
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public boolean containsAllBacteriologicAlgorithmObserver(Collection elements) {
		return bacteriologicAlgorithmObserver.containsAll(elements);
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public int bacteriologicAlgorithmObserverSize() {
		return bacteriologicAlgorithmObserver.size();
	}

	/**
	 * 
	 * @uml.property name="bacteriologicAlgorithmObserver"
	 */
	public fr.irisa.triskell.bacteria.framework.BacteriologicAlgorithmObserver[] bacteriologicAlgorithmObserverToArray() {
		return (fr.irisa.triskell.bacteria.framework.BacteriologicAlgorithmObserver[]) bacteriologicAlgorithmObserver
			.toArray(new fr.irisa.triskell.bacteria.framework.BacteriologicAlgorithmObserver[bacteriologicAlgorithmObserver
				.size()]);
	}

	public Subject() {
		bacteriologicAlgorithmObserver = new ArrayList();
	}

}
