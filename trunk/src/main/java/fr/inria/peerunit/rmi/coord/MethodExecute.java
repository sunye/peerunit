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
package fr.inria.peerunit.rmi.coord;

import java.rmi.RemoteException;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;

/**
* @author Eduardo Almeida.
* @version 1.0
* @since 1.0
* @see java.lang.Runnable 
* @see fr.inria.peerunit.Tester
* @see fr.inria.peerunit.parser.MethodDescription
*/
public class MethodExecute implements Runnable {
	private Tester tester;
	private MethodDescription md;
	
	/**
	 * 
	 * @param t the action's executor
	 * @param m the action which will be executed
	 */
	public MethodExecute(Tester t, MethodDescription m) {
		tester =t;
		md = m;
	}
	
	/**
	 * start the action's execution by a tester
	 */
	public void run() {
		try {
			tester.execute(md);
		} catch (RemoteException e) {			
			e.printStackTrace();
		}
	}

}
