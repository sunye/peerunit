package fr.inria.peerunit.exception;

public class AnnotationFailure  extends AssertionError{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AnnotationFailure(String message) {
		super (message);
		
	}
}
