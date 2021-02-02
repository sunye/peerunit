package fr.inria.mdca.util;

public class WrongOrderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WrongOrderException(int order, int elements) {
		super("Wrong order, touples of order "+order+" don't match the number of elements: "+elements);
	}

}
