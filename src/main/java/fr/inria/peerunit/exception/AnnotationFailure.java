package fr.inria.peerunit.exception;

/*
 * Thrown to indicate that an annotation is not well formed.
 * The public constructor provided by this class ensure that the annotation error returned by the invocation: 
 * new AnnotationFailure("message")
 */
public class AnnotationFailure  extends AssertionError{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/*
	 * Constructs an AnnotationFailure with its detail message derived from the specified string
	 * @param message - value to be used in constructing detail message
	 */
	public AnnotationFailure(String message) {
		super (message);
	}
}
