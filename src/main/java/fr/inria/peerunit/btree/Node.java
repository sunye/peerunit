package fr.inria.peerunit.btree;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import fr.inria.peerunit.MessageType;
import fr.inria.peerunit.util.BTreeNode;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Verdicts;
/**
 * 
 * @author Eduardo Almeida
 * @author Aboubakar Koïta 
 * @version 1.0
 * @since 1.0
 */
@Deprecated
public interface Node extends Remote {
    
    
	/**
	 * Receive a message from another Node.
	 * OK are sent only to Nodes (way up the tree)
	 * EXECUTE are sent to both Testers and Nodes (way down the tree)
 	 * FAIL and ERROR are sent only to Testers
 	 * REGISTER used by Testers to get their ID and by Nodes to store their Testers
	 * @param message Message received
	 * @param mdToExecute
	 * @throws RemoteException
	 */
	public void accept(MessageType message,MethodDescription mdToExecute) throws RemoteException;
	
	
	/**
	 * Associates a BTreeNode and a TreeElement to this Node
	 * @param abstractBTreeNode The BTreeNode to be associated to this Node
	 * @param te The TreeElement to be associated to this Node
	 * @throws java.rmi.RemoteException
	 */
	public void setElements(BTreeNode abstractBTreeNode,TreeElements te) throws RemoteException;
	
	
	/**
	 * Receives a list of verdicts from a child
	 * The children's verdicts are added to this node's verdict
	 * @param localVerdicts The child's verdicts to be added to this node's own verdicts
	 * @throws java.rmi.RemoteException	
	 */
	public void acceptVerdict(List<Verdicts> localVerdicts) throws RemoteException;

	/**
	 * Returns the IP addresses of the station hosting the node
	 * 
	 * @return a IP addresses
	 * @throws RemoteException
	 */
	public String getIP() throws RemoteException;
}
