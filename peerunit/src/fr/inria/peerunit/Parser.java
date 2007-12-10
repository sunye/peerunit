package fr.inria.peerunit;

import java.util.List;

import fr.inria.peerunit.parser.MethodDescription;

public interface Parser {
	/**
	 * Parse the class to extract the methods to be executed wrt to the Parsing implementation
	 * @param class
	 * @return List of methods to be executed
	 */
	public List<MethodDescription> parse(Class c) ;
	
	/**
	 * Veirifies if the method is the last one to be executed by its annotation
	 * @param method
	 * @return true if the method is the last one to be executed
	 */
	public boolean isLastMethod(String methodAnnotation);
	
	/**
	 * Sets the name of the peer that executes the methods
	 * @param peerName
	 */
	public void setPeerName(int peerName);
}
