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
package fr.inria.peerunit.exception;

/*
 * Thrown to indicate an peer unit test failure.
 */
public class PeerUnitFailure extends AssertionError {
	private static final long serialVersionUID = 1L;

	/*
	 * Constructs an PeerUnitFailure with no detail message. 
	 */
	public PeerUnitFailure() {
		super ();
	}
	
	/*
	 * Constructs an PeerUnitFailure with its detail message derived from the specified string
	 * @param message - value to be used in constructing detail message
	 */
	public PeerUnitFailure(String message) {
		super (message);
	}

}
