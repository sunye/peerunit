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

import fr.inria.peerunit.exception.TestException;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 * 
 * @see PeerUnitFailure
 *
 */
public class AssertionFailedError extends TestException {
	
	private static final long serialVersionUID= 1L;
	
	private String expected;
	private String actual;

	/**
	 * Constructs a assertion failed error.
	 * 
	 *
	 * @since 1.0
	 */
	public AssertionFailedError(){}



        public AssertionFailedError(String message) {
            super(message);
        }

	/**
	 * Constructs a assertion failed error.
	 * 
	 *
	 * @since 1.0
	 * @param message is the message corresponding to the error. 
	 */
	public AssertionFailedError(String message, String expected, String actual) {
		super(message);
		this.expected = expected;
		this.actual = actual;
	}
	
	@Override
	public String toString() {
		return String.format("Comparison Failure. expected: %s found: %s", expected, actual);
	}

}
