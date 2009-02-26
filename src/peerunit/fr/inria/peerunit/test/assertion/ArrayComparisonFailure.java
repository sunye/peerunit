package fr.inria.peerunit.test.assertion;

import java.util.ArrayList;
import java.util.List;

import fr.inria.peerunit.exception.PeerUnitFailure;

/**
 * Thrown when two array elements differ.
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 * 
 */
public class ArrayComparisonFailure extends PeerUnitFailure {

	private static final long serialVersionUID = 1L;
	private List<Integer> fIndices= new ArrayList<Integer>();
	private final String fMessage;
	private final AssertionError fCause;

	/**
	 * Constructs a new <code>ArrayComparisonFailure</code> with an error text and the array's
	 * dimension that was not equal. 
	 * 
	 * @since 1.0
	 * @param cause the exception that caused the array's content to fail the assertion test 
	 * @param index the array position of the objects that are not equal.
	 */
	public ArrayComparisonFailure(String message, AssertionError cause, int index) {
		fMessage= message;
		fCause= cause;
		addDimension(index);
	}

	/**
	 * Adds a dimenension in list <code>fIndices</code>.
	 * @since 1.0
	 *
	 * @param index is the index to add. 
	 */
	public void addDimension(int index) {
		fIndices.add(0, index);
	}

	@Override
	public String getMessage() {
		StringBuilder builder= new StringBuilder();
		if (fMessage != null)
			builder.append(fMessage);
		builder.append("arrays first differed at element ");
		for (int each : fIndices) {
			builder.append("[");
			builder.append(each);
			builder.append("]");
		}
		builder.append("; ");
		builder.append(fCause.getMessage());
		return builder.toString();
	}
	
	@Override 
	public String toString() {
		return getMessage();
	}
}
