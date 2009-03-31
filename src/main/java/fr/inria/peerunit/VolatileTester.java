package fr.inria.peerunit;

/**
 * This interface define a volatile <i>tester</i>, more  a <i>tester</i> who can be stopped 
 * during the testing.
 * 
 * @author Eduardo Almeida
 * @author Aboubakar Koïta
 * @version 1.0
 * @since 1.0 
 * @see fr.inria.peerunit.StorageTester
 * @see fr.inria.peerunit.rmi.tester.TesterImpl 
 */
public interface VolatileTester {
	
	/**
	 * Stop the <i>tester</i>.
	 */	
	public void kill() ;
}
