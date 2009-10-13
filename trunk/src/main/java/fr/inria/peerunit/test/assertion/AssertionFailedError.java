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

import fr.inria.peerunit.exception.PeerUnitFailure;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 * 
 * @see PeerUnitFailure
 *
 */
public class AssertionFailedError extends PeerUnitFailure {
	
	private static final long serialVersionUID= 1L;

	/**
	 * Constructs a assertion failed error.
	 * 
	 *
	 * @since 1.0
	 */
	public AssertionFailedError(){}

	/**
	 * Constructs a assertion failed error.
	 * 
	 *
	 * @since 1.0
	 * @param message is the message corresponding to the error. 
	 */
	public AssertionFailedError(String message) {
		super(message);
	}
}
