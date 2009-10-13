package fr.inria.peerunit;

import java.rmi.Remote;
import java.rmi.RemoteException;

import fr.inria.peerunit.parser.MethodDescription;

/**
 * This interface represent a <i>tester/i>. A <i>tester</i> is a component who control a peer 
 * to test and executes on him the <i>actions</i> of a <i>test case</i>.
 * @author Eduardo Almeida
 * @author Aboubakar Koita
 * @version 1.0
 * @since 1.0 
 * @see fr.inria.peerunit.rmi.tester.TesterImpl
 * @see fr.inria.peerunit.tree.TreeTesterImpl 
 */
public interface Tester extends Remote, StorageTester {

    /**
     * Sets the coordinator for this tester.
     *
     * @param coord
     */
    public void setCoordinator(Coordinator coord) throws RemoteException;

    /**
     * Execute a <i>test case action</i> thanks to it description.
     *
     * @param m is a instance of <tt>MathodDescription</tt> class containing all informations allowing
     *        the correct execution of the <i>test case action</i> that it describes.
     * @throws RemoteException because the method is distant
     */
    public void execute(MethodDescription m) throws RemoteException;

    /**
     * Return the <i>tester's</i> id.
     *
     * @throws RemoteException
     */
    public int getId() throws RemoteException;

    /**
     * Stop the <i>tester</i>.
     */
    public void kill() throws RemoteException;

    public void start() throws RemoteException;

}
