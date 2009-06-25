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
