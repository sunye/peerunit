package fr.inria.peerunit.rmi.coord;

import java.rmi.RemoteException;

import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;

public class MethodExecute implements Runnable {
	private Tester tester;
	private MethodDescription md;
	
	public MethodExecute(Tester t, MethodDescription m) {
		tester =t;
		md = m;
	}
	
	
	public void run() {
		try {
			tester.execute(md);
		} catch (RemoteException e) {			
			e.printStackTrace();
		}
	}

}
