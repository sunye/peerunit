/*
    This file is part of PeerUnit.

    Foobar is free software: you can redistribute it and/or modify
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
package fr.inria.peerunit.test.assertion;

import java.lang.reflect.Array;
import java.util.List;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 *
 */

public class Assert {
	
	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * {@link AssertionError} with the given message.
	 * @param condition is condition to be checked
	 *
	 * @since 1.0
	 */
	static public void assertTrue(String message, boolean condition) {
		if (!condition)
			fail(message);
	}

	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * {@link AssertionError} without a message.
	 * @param condition is condition to be checked
	 *
	 * @since 1.0
	 */
	static public void assertTrue(boolean condition) {
		assertTrue(null, condition);
	}

	/**
	 * Asserts that a condition is false. If it isn't it throws an
	 * {@link AssertionError} with the given message.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param condition condition to be checked
	 *
	 * @since 1.0
	 */
	static public void assertFalse(String message, boolean condition) {
		assertTrue(message, !condition);
	}

	/**
	 * Asserts that a condition is false. If it isn't it throws an
	 * {@link AssertionError} without a message.
	 * @param condition condition to be checked
	 *
	 * @since 1.0
	 */
	static public void assertFalse(boolean condition) {
		assertFalse(null, condition);
	}

	/**
	 * Fails a test with the given message.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @see AssertionError
	 *
	 * @since 1.0
	 */
	static public void fail(String message) {
		throw new AssertionError(message == null ? "" : message);
	}

	/**
	 * Fails a test with the given message.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @see AssertionError
	 *
	 * @since 1.0
	 */
	static public void inconclusive(String message) {
		throw new InconclusiveFailure(message == null ? "" : message);
	}
	
	/**
	 * Fails a test with no message.
	 *
	 * @since 1.0
	 */
	static public void fail() {
		fail(null);
	}
	
	/**
	 * Asserts that two objects are equal. If they are not, an {@link AssertionError} 
	 * is thrown with the given message. If <code>expected</code> and <code>actual</code>
	 * are <code>null</code>, they are considered equal.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param expected expected value
	 * @param actual actual value
	 *
	 * @since 1.0
	 */
	static public void assertEquals(String message, Object expected, Object actual) {
		if (expected == null && actual == null){
			return;
		}
		if (expected != null && actual == null){
			String cleanMessage= message == null ? "" : message;
			throw new InconclusiveFailure(cleanMessage);
		} else if (expected != null && isEquals(expected, actual)){
			return;
		} else if (expected instanceof String && actual instanceof String) {
			String cleanMessage= message == null ? "" : message;
			throw new ComparisonFailure(cleanMessage, (String)expected, (String)actual);			
		} else {
			failNotEquals(message, expected, actual);
		}
	}

	/**
	 * Asserts that two objects are equal. If they are not, an {@link AssertionError} 
	 * without a message is thrown. If <code>expected</code> and <code>actual</code>
	 * are <code>null</code>, they are considered equal.
	 * @param expected expected value
	 * @param actual the value to check against <code>expected</code>
	 *
	 * @since 1.0
	 */
	static public void assertEquals(Object expected, Object actual) {
		assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two object arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message. If <code>expecteds</code> and
	 *  <code>actuals</code> are <code>null</code>, they are considered equal.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values.
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(String message, Object[] expecteds,
			Object[] actuals) throws ArrayComparisonFailure {
		internalArrayEquals(message, expecteds, actuals);
	}
	
	/**
	 * Asserts that two object arrays are equal. If they are not, an {@link AssertionError} 
	 * is thrown.  If <code>expected</code> and <code>actual</code> are <code>null</code>, 
	 * they are considered equal.
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(Object[] expecteds, Object[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}

	/**
	 * 
	 * Asserts that two object arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message. If <code>expecteds</code> and
	 *  <code>actuals</code> are <code>null</code>, they are considered equal.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values.
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(String message, byte[] expecteds,
			byte[] actuals) throws ArrayComparisonFailure {
		internalArrayEquals(message, expecteds, actuals);
	}

	/**
	 * 
	 * Asserts that two object arrays are equal. If they are not, an {@link AssertionError} 
	 * is thrown.  If <code>expected</code> and <code>actual</code> are <code>null</code>, 
	 * they are considered equal.
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(byte[] expecteds, byte[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	
	/**
	 * 
	 * Asserts that two object arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message. If <code>expecteds</code> and
	 *  <code>actuals</code> are <code>null</code>, they are considered equal.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values.
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(String message, char[] expecteds,
			char[] actuals) throws ArrayComparisonFailure {
		internalArrayEquals(message, expecteds, actuals);
	}

	/**
	 * 
	 * Asserts that two object arrays are equal. If they are not, an {@link AssertionError} 
	 * is thrown.  If <code>expected</code> and <code>actual</code> are <code>null</code>, 
	 * they are considered equal.
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(char[] expecteds, char[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	
	/**
	 * 
	 * Asserts that two object arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message. If <code>expecteds</code> and
	 *  <code>actuals</code> are <code>null</code>, they are considered equal.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values.
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(String message, short[] expecteds,
			short[] actuals) throws ArrayComparisonFailure {
		internalArrayEquals(message, expecteds, actuals);
	}

	/**
	 * 
	 * Asserts that two object arrays are equal. If they are not, an {@link AssertionError} 
	 * is thrown.  If <code>expected</code> and <code>actual</code> are <code>null</code>, 
	 * they are considered equal.
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(short[] expecteds, short[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	
	/**
	 * 
	 * Asserts that two object arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message. If <code>expecteds</code> and
	 *  <code>actuals</code> are <code>null</code>, they are considered equal.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values.
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(String message, int[] expecteds,
			int[] actuals) throws ArrayComparisonFailure {
		internalArrayEquals(message, expecteds, actuals);
	}

	/**
	 * 
	 * Asserts that two object arrays are equal. If they are not, an {@link AssertionError} 
	 * is thrown.  If <code>expected</code> and <code>actual</code> are <code>null</code>, 
	 * they are considered equal.
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(int[] expecteds, int[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}

	/**
	 * 
	 * Asserts that two object arrays are equal. If they are not, an {@link AssertionError} 
	 * is thrown.  If <code>expected</code> and <code>actual</code> are <code>null</code>, 
	 * they are considered equal.
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(long[] expecteds, long[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}
	
	/**
	 * 
	 * Asserts that two object arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message. If <code>expecteds</code> and
	 *  <code>actuals</code> are <code>null</code>, they are considered equal.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values.
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	public static void assertArrayEquals(String message, long[] expecteds,
			long[] actuals) throws ArrayComparisonFailure {
		internalArrayEquals(message, expecteds, actuals);
	}
	
	/**
	 * Asserts that two doubles or floats are equal to within a positive delta. If they
	 * are not, an {@link AssertionError} is thrown with the given message. If the
	 * expected value is infinity then the delta value is ignored. NaNs are
	 * considered equal:
	 * <code>assertEquals(Double.NaN, Double.NaN, *)</code> passes
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param expected expected value
	 * @param actual the value to check against <code>expected</code>
	 * @param delta the maximum delta between <code>expected</code> and <code>actual</code> for which 
	 * both numbers are still considered equal.
	 *
	 * @since 1.0
	 */
	static public void assertEquals(String message, double expected, double actual, double delta) {
		if (Double.compare(expected, actual) == 0)
			return;
		if (!(Math.abs(expected - actual) <= delta))
			failNotEquals(message, new Double(expected), new Double(actual));
	}

	/**
	 * Asserts that two doubles or floats are equal to within a positive delta. If they
	 * are not, an {@link AssertionError} is thrown. If the
	 * expected value is infinity then the delta value is ignored.NaNs are
	 * considered equal:
	 * <code>assertEquals(Double.NaN, Double.NaN, *)</code> passes
	 * @param expected expected value
	 * @param actual the value to check against <code>expected</code>
	 * @param delta the maximum delta between <code>expected</code> and <code>actual</code> for which 
	 * both numbers are still considered equal.
	 *
	 * @since 1.0
	 */
	static public void assertEquals(double expected, double actual, double delta) {
		assertEquals(null, expected, actual, delta);
	}

	/**
	 * Asserts that an object isn't null. If it is an {@link AssertionError} is
	 * thrown with the given message.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param object Object to check or <code>null</code>
	 *
	 * @since 1.0
	 */
	static public void assertNotNull(String message, Object object) {
		assertTrue(message, object != null);
	}

	/**
	 * Asserts that an object isn't null. If it is an {@link AssertionError} is
	 * thrown.
	 * @param object Object to check or <code>null</code>
	 *
	 * @since 1.0
	 */
	static public void assertNotNull(Object object) {
		assertNotNull(null, object);
	}
	
	/**
	 * Asserts that an object is null. If it is not, an {@link AssertionError} is
	 * thrown with the given message.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param object Object to check or <code>null</code>
	 *
	 * @since 1.0
	 */
	static public void assertNull(String message, Object object) {
		assertTrue(message, object == null);
	}

	/**
	 * Asserts that an object is null. If it isn't an {@link AssertionError} is
	 * thrown.
	 * @param object Object to check or <code>null</code>
	 *
	 * @since 1.0
	 */
	static public void assertNull(Object object) {
		assertNull(null, object);
	}
	
	/**
	 * Asserts that two objects refer to the same object. If they are not, an
	 * {@link AssertionError} is thrown with the given message.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param expected the expected object
	 * @param actual the object to compare to <code>expected</code>
	 *
	 * @since 1.0
	 */
	static public void assertSame(String message, Object expected, Object actual) {
		if (expected == actual)
			return;
		failNotSame(message, expected, actual);
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not the
	 * same, an {@link AssertionError} without a message is thrown.
	 * @param expected the expected object
	 * @param actual the object to compare to <code>expected</code>
	 *
	 * @since 1.0
	 */
	static public void assertSame(Object expected, Object actual) {
		assertSame(null, expected, actual);
	}

	/**
	 * Asserts that two objects do not refer to the same object. If they do
	 * refer to the same object, an {@link AssertionError} is thrown with the given
	 * message.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param unexpected the object you don't expect
	 * @param actual the object to compare to <code>unexpected</code>
	 *
	 * @since 1.0
	 */
	static public void assertNotSame(String message, Object unexpected, Object actual) {
		if (unexpected == actual)
			failSame(message);
	}

	/**
	 * Asserts that two objects do not refer to the same object. If they do
	 * refer to the same object, an {@link AssertionError} without a message is thrown.
	 * @param unexpected the object you don't expect
	 * @param actual the object to compare to <code>unexpected</code>
	 *
	 * @since 1.0
	 */
	static public void assertNotSame(Object unexpected, Object actual) {
		assertNotSame(null, unexpected, actual);
	}

	/**
	 * Asserts that two lists are the same. If they are not equal, 
	 * an {@link AssertionError} with the given message is thrown.
	 * @param expecteds is the expected list. 
	 * @param actuals is the actual list. 
	 * @param message is the message corresponding to the error.
	 *
	 * @since 1.0
	 */
	public static void assertListEquals(String message,List<String> expecteds, List<String> actuals) {
		for (String expected : expecteds) {
			if(!actuals.contains(expected)){ 
				failNotEquals(message, expecteds, actuals);
			}
		}						
	}

	/**
	 * Asserts that two object arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message. If <code>expecteds</code> and
	 *  <code>actuals</code> are <code>null</code>, they are considered equal.
	 * @param message the identifying message or <code>null</code> for the {@link AssertionError}
	 * @param expecteds Object array or array of arrays (multi-dimensional array) with expected values.
	 * @param actuals Object array or array of arrays (multi-dimensional array) with actual values
	 *
	 * @since 1.0
	 */
	private static void internalArrayEquals(String message, Object expecteds,
			Object actuals) throws ArrayComparisonFailure {
		if (expecteds == actuals)
			return;
		String header = message == null ? "" : message + ": ";
		if (expecteds == null)
			fail(header + "expected array was null");
		if (actuals == null){
			String cleanMessage= message == null ? "" : message;
			throw new InconclusiveFailure(cleanMessage);
			//fail(header + "actual array was null");
		}
		int actualsLength= Array.getLength(actuals);
		int expectedsLength= Array.getLength(expecteds);
		if (actualsLength != expectedsLength){
			String cleanMessage= message == null ? "" : message;
			throw new InconclusiveFailure(cleanMessage);
			//fail(header + "array lengths differed, expected.length=" + expectedsLength + " actual.length=" + actualsLength);
		}
	
		for (int i= 0; i < expectedsLength; i++) {
			Object expected= Array.get(expecteds, i);
			Object actual= Array.get(actuals, i);
			if (expected.getClass().isArray() && actual.getClass().isArray()) {
				try {
					internalArrayEquals(message, expected, actual);
				} catch (ArrayComparisonFailure e) {
					e.addDimension(i);
					throw e;
				}
			} else
				try {
					assertEquals(expected, actual);
				} catch (AssertionError e) {
					throw new ArrayComparisonFailure(header, e, i);
				}
		}
	}

	static private void failSame(String message) {
		String formatted= "";
		if (message != null)
			formatted= message + " ";
		fail(formatted + "expected not same");
	}

	static private void failNotSame(String message, Object expected, Object actual) {
		String formatted= "";
		if (message != null)
			formatted= message + " ";
		fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
	}

	static private void failNotEquals(String message, Object expected, Object actual) {
		fail(format(message, expected, actual));
	}

	private static boolean isEquals(Object expected, Object actual) {
		if (expected instanceof Number && actual instanceof Number)
			return ((Number) expected).longValue() == ((Number) actual).longValue();
		return expected.equals(actual);
	}

	static String format(String message, Object expected, Object actual) {
		String formatted= "";
		if (message != null && ! message.equals(""))
			formatted= message + " ";
		return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
	}
}
