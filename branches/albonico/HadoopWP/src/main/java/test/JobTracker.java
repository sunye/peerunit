package test;

/**
 * @author albonico  
 */

// My classes
//import examples.PiEstimator;
import load.StartClusterParent;

// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.parser.AfterClass;

// Java classes
import java.io.IOException;
import java.rmi.RemoteException;

public class JobTracker extends StartClusterParent {

	public static void main(String[] args) {


		StartClusterParent scp = new StartClusterParent();

    		scp.startJobTracker();

	}

}
