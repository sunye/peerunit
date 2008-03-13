package fr.inria.peerunit;

import java.util.List;

import fr.inria.peerunit.parser.MethodDescription;

public interface Executor {
	/**
	 * Parse the class to extract the methods to be executed wrt to the Parsing implementation
	 * @param class
	 * @return List of methods to be executed
	 */
	public List<MethodDescription> register(Class<? extends TestCase> c) ;

	/**
	 * Verifies if the method is the last one to be executed by its annotation
	 * @param method
	 * @return true if the method is the last one to be executed
	 */
	public boolean isLastMethod(String methodAnnotation);

}
