package fr.inria.peerunit;

/**
 * @author albonico  
 */

// My classes
//import examples.PiEstimator;
//import load.StartClusterParent;

// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.parser.AfterClass;

// Java classes
import java.io.IOException;


public class TestStartCluster extends StartClusterParent {

	
	@TestStep(order=1, timeout = 150000, range = "0")
    public void startNameNode() throws IOException, InterruptedException {

    	initNN();

    }
	

    @TestStep(order=2, timeout = 100000, range = "*")
    public void startDataNode() throws IOException, InterruptedException {

    	initDN();

    }

    @TestStep(order=3, timeout = 100000, range = "0")
    public void startJobTracker() throws Exception {
    	
    	initJT();
    	
   } 

   @TestStep(order=4, timeout = 100000, range = "*")
    public void startTaskTracker() throws Exception {

	 	initTT();

    }
   
   @TestStep(order=15, timeout=30000, range="*")
    public void stopSlaveServices() throws IOException, InterruptedException {
    
    	if (dnThread.isAlive()) {
	    	log.info("Stopping Datanode...");
	    	dn.shutdown();
	    	dnThread.interrupt();
    	}
    	
    	if (ttThread.isAlive()) {
		log.info("Stopping TaskTracker...");
		TTracker.shutdown();
		ttThread.interrupt();
    	}

    	
    }
    	
    @TestStep(order=16, timeout=30000, range="0")
    public void stopMasterServices() throws IOException, InterruptedException {
    	
    	if (jtThread.isAlive()) {
	    	log.info("Stopping JobTracker...");
	    	JTracker.stopTracker();
	    	jtThread.interrupt();
    	}
    	
    	if (nnThread.isAlive()) {
	    	log.info("Stopping NameNode...");
	    	nn.stop();
	    	nnThread.interrupt();
    	}

    }
    
    @AfterClass(range="*", timeout = 100000)
    public void stopCluster() throws IOException {
    	log.info("End of test case!");
    	
    }


}
