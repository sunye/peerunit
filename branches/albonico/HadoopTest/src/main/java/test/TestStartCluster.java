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

public class TestStartCluster extends StartClusterParent {

/*	
	@TestStep(order=1, timeout = 10000, range = "0")
    public void startNameNode() throws IOException, InterruptedException {

    	initNN();

    }

	

    @TestStep(order=2, timeout=100000, range="1")
    public void startSNameNode() throws IOException, InterruptedException {

    	initSNN();

    }

    @TestStep(order=2, timeout = 100000, range = "*")
    public void startDataNode() throws IOException, InterruptedException {

    	initDN();

    }

*/    
    @TestStep(order=2, timeout = 100000, range = "0")
    public void startJobTracker() throws Exception {

    	initJT();
/*
    	log.info("Starting JobTracker!");

        String command = "/home/michel/hadoop-0.21.0/bin/start-job.sh";
        final Process process = Runtime.getRuntime().exec(command);
*/
    	
    }
/*
    @TestStep(order=4, timeout = 100000, range = "*")
    public void startTaskTracker() throws Exception {
    
    	log.info("Starting TaskTracker!");
    	
        String command = "/home/michel/hadoop-0.21.0/bin/start-task.sh";
        final Process process = Runtime.getRuntime().exec(command);

	 	initTT();

    }

    @TestStep(order=5, timeout = 440000, range = "0")
    public void sendJob() throws Exception, InterruptedException {

    	runPiEstimator piest = new runPiEstimator();
	    Thread piThread = new Thread(piest);
	    piThread.start();
	    piThread.join();
   
    }

    @AfterClass(range="*", timeout = 100000)
    public void stopCluster() {

    }
*/
}
