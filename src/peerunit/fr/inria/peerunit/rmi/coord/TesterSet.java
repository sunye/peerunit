package fr.inria.peerunit.rmi.coord;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.inria.peerunit.Tester;


public class TesterSet {
	private List<Tester> peers = Collections.synchronizedList(new ArrayList<Tester>());

	public void add(Tester t) {
		synchronized (this) {
			if (!peers.contains(t)) {
				peers.add(t);
			}
		}
	}

	public void remove(Tester t){
		synchronized (this) {
			if (peers.contains(t)) {
				peers.remove(t);
			}
		}
	}

	public List<Tester> getTesters(){
		return peers;
	}

	public int getPeersQty(){
		return peers.size();
	}

	public boolean isEmpty(){
		if(peers.isEmpty()){
			return true;
		}else{
			return false;
		}
	}

	public void clear(){
		peers.clear();
	}
}
