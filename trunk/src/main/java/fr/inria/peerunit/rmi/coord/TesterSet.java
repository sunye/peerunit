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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.inria.peerunit.Tester;

/**
* @author Eduardo Almeida.
* @version 1.0
* @since 1.0
* @see java.lang.Runnable 
* @see fr.inria.peerunit.Tester
* @see java.util.Collections#synchronizedList(List)
*/

@Deprecated
public class TesterSet {
	private List<Tester> peers = Collections.synchronizedList(new ArrayList<Tester>());

	/**
	 * Adds a tester at the testers's list
	 * 
	 * @param t tester which will be added
	 */
	public void add(Tester t) {
		synchronized (this) {
			if (!peers.contains(t)) {
				peers.add(t);
			}
		}
	}

	/**
	 * Removes a tester at the testers's list
	 * 
	 * @param t tester which will be removed
	 */
	public void remove(Tester t){
		synchronized (this) {
			if (peers.contains(t)) {
				peers.remove(t);
			}
		}
	}

	/**
	 * 
	 * @return the testers's list
	 */
	public List<Tester> getTesters(){
		return peers;
	}

	/**
	 * 
	 * @return the size of the testers's list
	 */
	public int size(){
		return peers.size();
	}

	/**
	 * 
	 * @return true if the testers's list is empty otherwise false 
	 */
	public boolean isEmpty(){
		if(peers.isEmpty()){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Clears the testers's list 
	 */
	public void clear(){
		peers.clear();
	}
}
