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
