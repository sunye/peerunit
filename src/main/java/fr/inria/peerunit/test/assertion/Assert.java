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
package fr.inria.peerunit.test.assertion;

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
        if (!condition) {
            fail(message);
        }
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
        throw new AssertionFailedError(message == null ? "" : message);
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
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            String cleanMessage = message == null ? "" : message;
            throw new InconclusiveFailure(cleanMessage);
        } else if (expected != null && isEquals(expected, actual)) {
            return;
        } else if (expected instanceof String && actual instanceof String) {
            String cleanMessage = message == null ? "" : message;
            throw new AssertionFailedError(cleanMessage, (String) expected, (String) actual);
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
     * Asserts that two lists are the same. If they are not equal,
     * an {@link AssertionError} with the given message is thrown.
     * @param expecteds is the expected list.
     * @param actuals is the actual list.
     * @param message is the message corresponding to the error.
     *
     * @since 1.0
     */
    public static void assertListEquals(String message, List<String> expecteds, List<String> actuals) {
        for (String expected : expecteds) {
            if (!actuals.contains(expected)) {
                failNotEquals(message, expecteds, actuals);
            }
        }
    }

    static private void failNotEquals(String message, Object expected, Object actual) {
        fail(format(message, expected, actual));
    }

    private static boolean isEquals(Object expected, Object actual) {
        if (expected instanceof Number && actual instanceof Number) {
            return ((Number) expected).longValue() == ((Number) actual).longValue();
        }
        return expected.equals(actual);
    }

    static String format(String message, Object expected, Object actual) {
        String formatted = "";
        if (message != null && !message.equals("")) {
            formatted = message + " ";
        }
        return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
    }
}
